type channel = string;;

type action =
| Tau
| In of channel
| Out of channel;;

type process =
| Nil
| Prefix of action * process
| Choice of process * process
| Parallel of process * process
| Restrict of process * channel list
| Rename of process * (channel * channel) list
| Recur of char * process
| Var of char;;

let rename_action rel = function
  | Tau -> Tau
  | In channel ->
    let o,n = List.find (fun (o,n) -> o = channel) rel in
    In n
  | Out channel ->
    let o,n = List.find (fun (o,n) -> o = channel) rel in
    Out n;;

let channel_of = function
  | Tau -> failwith "Impossible get the channel of an internal action"
  | In c -> c
  | Out c -> c;;

let rec unfold_process var_name to_insert =
  let rec unfold = function
    | Nil -> Nil
    | Prefix (action, process) ->
      Prefix (action, unfold process)
      (* begin *)
      (* 	match action with *)
      (* 	| Tau -> unfold process *)
      (* 	| other ->       *)
    (* end *)
    | Choice (p,q) ->
      Choice (unfold p, unfold q)
    | Parallel (p, q) ->
      Parallel (unfold p, unfold q)
    | Restrict (p, channels) ->
      Restrict (unfold p, channels)
    | Rename (p, rel) ->
      Rename (unfold p, rel)
    | Recur (a, p) ->
      Recur (a, unfold p)
    | Var var_identifier as another_var->
      if var_identifier = var_name
      then to_insert
      else another_var
  in function to_unfold -> unfold to_unfold;;

let rec string_of_process =
  let string_of_channel = function c -> "" ^ c in
  let wrap_in_brackets = function s -> "(" ^ s ^")" in
  let string_of_action = function
    | Tau -> "tau"
    | In c -> c ^ "?"
    | Out c -> c ^ "!" in
  function
  | Nil -> "nil"
  | Prefix (action, process) ->
    (string_of_action action) ^ "." ^ (string_of_process process)
  | Choice (p,q) ->
    wrap_in_brackets ((string_of_process p) ^ " + " ^ (string_of_process q))
  | Parallel (p, q) ->
    wrap_in_brackets ((string_of_process p) ^ " | " ^ (string_of_process q))
  | Restrict (p, channels) ->
    let string_of_channels =
      function channel_list -> List.fold_left
	(fun cum current -> cum ^ ", " ^ current)
	"" channel_list
    in
    "(" ^ (string_of_process p) ^ ")\\{" ^
      (string_of_channels (List.map string_of_channel channels)) ^ "}"
  | Rename (p, rel) ->
    let pairs_as_strings = 
      List.map (fun (s,d) -> "("^(string_of_channel s)^", "^(string_of_channel d)^")")
	rel in
    (wrap_in_brackets (string_of_process p)) ^ "[" ^
      List.fold_left
      (fun cum current -> cum ^ ", " ^ current)
      "" pairs_as_strings
  | Recur (var_name, p) ->
    "Rec" ^ (Char.escaped var_name) ^ (wrap_in_brackets (string_of_process p))
  | Var var_name -> (Char.escaped var_name);;

let rec next =
  function
  | Prefix (action,p) -> [action,p]
  | Choice (p,q) -> (next p) @ (next q)
  | Parallel (p,q) ->
    let next_of_p = next p in
    let next_of_q = next q in
    let rec sync_actions cum_sync_actions = function
      | [] -> cum_sync_actions
      | (Tau, p') :: cdr ->
	sync_actions cum_sync_actions cdr
      | (In c, p') :: cdr ->
	let out_actions = List.filter
	  (fun (b, q') ->
	    match b with
	    | Out a when a = c -> true
	    | _ -> false)
	  next_of_q in	
	sync_actions
	  ((List.map (fun (b, q') -> Tau, Parallel (p', q'))
	      out_actions) @ cum_sync_actions)
	  cdr
      | (Out c, p') :: cdr ->
	let in_actions = List.filter
	  (fun (b, q') ->
	    match b with
	    | In a when a = c -> true
	    | _ -> false)
	  next_of_q in	
	sync_actions
	  ((List.map (fun (b, q') -> Tau, Parallel (p', q'))
	      in_actions) @ cum_sync_actions)
	  cdr in
    (List.map (fun (a, p') -> a, Parallel (p', q)) next_of_p) @
      (List.map (fun (a, q') -> a, Parallel (p, q')) next_of_q) @
      (sync_actions [] next_of_p)
  | Restrict (p, channels) -> 
    let allowed_nexts = List.filter
      (fun (a,p) ->
	match a with
	| Tau -> true
	| other -> not (List.mem (channel_of other) channels))
      (next p) in
    List.map (fun (a, p) -> a, Restrict (p, channels))
      allowed_nexts
  | Rename (p, rel) ->
    List.map (fun (a,p) -> (rename_action rel a), Rename (p, rel))
      (next p)
  | Recur (var_name, p) ->
    List.map (fun (a,q) -> a, (unfold_process var_name p q))
      (next p)
  | _ -> [];;

type formula =
| True
| False
| Not of formula
| Or of formula * formula
| And of formula * formula
| Box of (action -> bool) * formula
| Diamond of (action -> bool) * formula
| Min of string * process list * formula
| Max of string * process list * formula
| VarFormula of string;;

let rec unfold_formula var_name to_insert =
  let rec unfold_in = function
    | Not f -> Not (unfold_in f)
    | Or (f,g) -> Or (unfold_in f, unfold_in g)
    | And (f,g) -> And (unfold_in f, unfold_in g)
    | Box (pred, f) -> Box (pred, (unfold_in f))
    | Diamond (pred, f) -> Diamond (pred, (unfold_in f))
    | Min (var, procs, f) -> Min (var, procs, (unfold_in f))
    | Max (var, procs, f) -> Max (var, procs, (unfold_in f))
    | VarFormula var as original->
      if var = var_name
      then to_insert
      else original
    | other -> other
  in
  function to_unfold -> unfold_in to_unfold;;

let rec sat p = function
  | True -> true
  | Not f -> not (sat p f)
  | Or (f,g) -> (sat p f) || (sat p g)
  | And (f,g) -> (sat p f) && (sat p g)
  | Box (pred, f) ->
    let pred_matchers = List.find_all
      (fun (a, p') ->
	match a with
	| others -> pred a) (next p) in
    List.for_all (fun (a, p') -> sat p' f) pred_matchers
  | Diamond (pred, f) ->
    let pred_matchers = List.find_all
      (fun (a, p') ->
	match a with
	| others -> pred a) (next p) in
    List.exists (fun (a, p') -> sat p' f) pred_matchers
  | Min (var_name, seen_procs, f) ->
    if List.mem p seen_procs
    then false
    else sat p (unfold_formula var_name (Min (var_name, (p::seen_procs), f)) f)
  | Max (var_name, seen_procs, f) ->
    if List.mem p seen_procs
    then true
    else sat p (unfold_formula var_name (Max (var_name, (p::seen_procs), f)) f)
  | _ -> false;;

let var_name = 'X' in
let unfold_into = Prefix (In "a", Parallel (Prefix (Out "b", Var var_name), Var var_name)) in
let rec_process = Recur (var_name, unfold_into) in
string_of_process (unfold_process var_name rec_process unfold_into);;

let pred = function action ->
  match action with
  | In "a" -> true
  | other -> false in
let rec_formula =
  Box (
    pred,
    Or (Diamond (pred, True), VarFormula "X")
  ) in
unfold_formula "X" rec_formula rec_formula;;


let process_s = Recur ('X', Prefix (In "a", Prefix (In "b", Recur ('Y', Choice (Prefix (In "b", Var 'Y'), Prefix (In "a", Var 'X')))))) in
let s_not_bisimilar_t_formula =
  Box (
    (fun a -> (channel_of a) = "a"),
    True      
  ) in
sat process_s s_not_bisimilar_t_formula;;

(** Entry point of the program*)
let not_bisimilar_processes_example = function () ->
  let process_s = Recur (
    'X',
    Prefix (In "a",
	    Prefix (In "b",
		    Recur ('Y',
			   Choice (Prefix (In "b", Var 'Y'),
				   Prefix (In "a", Var 'X')))))) in
  let process_t = Recur (
    'X',
    Prefix (In "a",
	    Recur ('Y',
		   Choice (Prefix (In "b", Var 'Y'),
			   Prefix (In "b", Prefix (In "a",
						   Var 'X')))))) in
  let process_v =
    let v2 = Recur ('Y',
		    Choice (Prefix (In "b", Var 'Y'),
			    Prefix (In "a", Var 'X'))) in
    Recur ('X',
	   Prefix (In "a",
		   Choice (
		     Prefix (In "b", Prefix (In "b", v2)),
		     Prefix (In "b", v2)))) in
  let s_not_bisimilar_t_formula =
    Box (
      (fun a -> (channel_of a) = "a"),
      Box (
	(fun a -> (channel_of a) = "b"),
	Diamond (
	  (fun a -> (channel_of a) = "b"),
	  True))) in
  let s_not_bisimilar_v_formula =
    Box (
      (fun a -> (channel_of a) = "a"),
      Diamond (
	(fun a -> (channel_of a) = "b"),
	Box (
	  (fun a -> (channel_of a) = "a"),
	  False))) in
  let t_not_bisimilar_v_formula =
    Box (
      (fun a -> (channel_of a) = "a"),
      Diamond (
	(fun a -> (channel_of a) = "b"),
	Box (
	  (fun a -> (channel_of a) = "b"),
	  False))) in
  (* let datas_filename = Sys.argv.(1) in *)
  (* let nodes_in_each_tree = Sys.argv.(2) in *)
  print_string ("Process s: " ^ string_of_process process_s);
  print_newline ();  
  print_string ("Process t: " ^ string_of_process process_t);
  print_newline ();  
  print_string ("Process v: " ^ string_of_process process_v);
  print_newline ();
  print_newline ();
  let sat_result first_descr second_descr f_descr first second f =
    let verb_of_verification_result = function
      | true -> " is "
      | false -> " isn't " in
    let verify_formula_on p = sat p f in
    let first_sat_result = verify_formula_on first in
    let second_sat_result = verify_formula_on second in
    print_string (first_descr ^
		    verb_of_verification_result (first_sat_result = second_sat_result) ^
		    " bisimilar to " ^
		    second_descr ^
		    " due to formula f= " ^ f_descr);
    print_newline ();
    print_string (first_descr ^ " satisfy f: " ^
		    string_of_bool (first_sat_result));
    print_newline ();
    print_string (second_descr ^ " satisfy f: " ^
		    string_of_bool (second_sat_result));
    print_newline () in
  sat_result "s" "t" "[a][b]<a>tt" process_s process_t s_not_bisimilar_t_formula;
  print_newline ();
  print_newline ();
  sat_result "s" "v" "[a]<b>[a]ff" process_s process_v s_not_bisimilar_v_formula;
  print_newline ();
  print_newline ();
  sat_result "t" "v" "[a]<b>[b]ff" process_t process_v t_not_bisimilar_v_formula;;

let car_train_crossing_example = function () ->
  let road = Recur ('X',
		    Prefix (In "car",
			    Prefix (In "up",
				    Prefix (Out "car_cross",
					    Prefix (Out "down", Var 'X'))))) in
  let rail = Recur ('Y',
		    Prefix (In "train",
			    Prefix (In "green",
				    Prefix (Out "train_cross",
					    Prefix (Out "red", Var 'Y'))))) in
  let signal = Recur ('Z',
		      Choice ((Prefix (Out "green",
				       Prefix (In "red", Var 'Z'))),
			      (Prefix (Out "up",
				       Prefix (In "down", Var 'Z'))))) in
  let crossing =
    Restrict (Parallel (Parallel (road, rail), signal),
	      ["green"; "red"; "up"; "down"]) in
  let safety_formula = Max ("Z", [],
			    And (Or (
			      Box (
				(fun a ->
				  match a with
				  | Tau -> false
				  | other ->
				    (channel_of other) = "train_cross"),
				False),
			      Box (
				(fun a ->
				  match a with
				  | Tau -> false
				  | other ->
				    (channel_of other) = "car_cross"),
				False)),
				 Box ((fun a -> true),
				      VarFormula "Z"))) in
  let liveness_formula = Min ("Z", [],
			      Or (
				Diamond (
				  (fun a ->
				    match a with
				    | Tau -> false
				    | other ->
				      (channel_of other) = "train_cross"),
				  True),
				Diamond ((fun a -> true),
					 VarFormula "Z"))) in
  let if_car_approaches_eventually_it_crosses_formula =
    Max ("Z", [],
	 And (
	   Box (
	     (fun a ->
	       match a with
	       | Tau -> false
	       | other ->
		 (channel_of other) = "car"),
	     Min ("Y", [],
		  And (
		    Diamond ((fun a -> true), True),
		    Box (
		      (fun a ->
			match a with
			| Tau -> false
			| other ->
			  not ((channel_of other) = "car_cross")),
		      VarFormula "Y")))),
	   Box (
	     (fun a ->
	       match a with
	       | Tau -> false
	       | other ->
		 not ((channel_of other) = "car")),
	     VarFormula "Z"))) in
  print_string ("Process road: " ^ string_of_process road);
  print_newline ();  
  print_string ("Process rail: " ^ string_of_process rail);
  print_newline ();
  print_string ("Process signal: " ^ string_of_process signal);
  print_newline ();  
  print_string ("Process crossing: " ^ string_of_process crossing);
  print_newline ();  
  print_string (string_of_process crossing);
  print_newline ();
  print_string ("Process crossing satisfy maxZ(([train_cross]ff \
 or [car_cross]ff) and [Act]Z): " ^
		   (string_of_bool (sat crossing safety_formula)));
  print_newline ();
  print_string ("Process crossing satisfy minZ(<train_cross>tt or <Act>Z): " ^
		   (string_of_bool (sat crossing liveness_formula)));
  print_newline ();
  print_string ("Process crossing satisfy maxZ([car](minY(<Act>tt and [-car_cross]Y)) and  [-car]Z): " ^
		   (string_of_bool
		      (sat crossing if_car_approaches_eventually_it_crosses_formula)));;  

let _ =
  print_string "--------not bisimilar processes example------------";
  print_newline ();
  not_bisimilar_processes_example ();
  print_newline ();
  print_newline ();
  print_string "--------car train crossing example------------";
  print_newline ();
  car_train_crossing_example();
  print_newline ();;

let restricted = Restrict (Prefix (In "a", Var 'X'), ["b"]) in
let p = Recur ('X', restricted) in
unfold_process 'X' p restricted;;
