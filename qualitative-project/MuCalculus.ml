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
  | Prefix (action,p) -> [(action,p)]
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
	    | Out c -> true
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
	    | In c -> true
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
      (fun (a,p) -> not (List.mem (channel_of a) channels))
      (next p) in
    List.map (fun (a, p) -> a, Restrict (p, channels))
      allowed_nexts
  | Rename (p, rel) ->
    List.map (fun (a,p) -> a, Rename (p, rel))
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
| Var of string;;

let rec unfold_formula var_name to_insert =
  let rec unfold_in = function
    | Not f -> Not (unfold_in f)
    | Or (f,g) -> Or (unfold_in f, unfold_in g)
    | And (f,g) -> And (unfold_in f, unfold_in g)
    | Box (pred, f) -> Box (pred, (unfold_in f))
    | Diamond (pred, f) -> Diamond (pred, (unfold_in f))
    | Min (var, procs, f) -> Min (var, procs, (unfold_in f))
    | Max (var, procs, f) -> Max (var, procs, (unfold_in f))
    | Var var as original->
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
      (fun (a, p') -> pred a) (next p) in
    List.for_all (fun (a, p') -> sat p' f) pred_matchers
  | Diamond (pred, f) ->
    let pred_matchers = List.find_all
      (fun (a, p') -> pred a) (next p) in
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

(* let var_name = 'X' in *)
(* let unfold_into = Prefix (In "a", Parallel (Prefix (Out "b", Var var_name), Var var_name)) in *)
(*   let rec_process = Recur (var_name, unfold_into) in *)
(*   string_of_process (unfold_process var_name rec_process unfold_into);; *)

let pred = function action ->
  match action with
  | In "a" -> true
  | other -> false in
let rec_formula =
  Box (
    pred,
    Or (Diamond (pred, True), Var "X")
  ) in
unfold_formula "X" rec_formula rec_formula;;
