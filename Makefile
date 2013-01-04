pdf: 
	pdflatex Elaborato.tex

complete: 
	pdflatex Elaborato.tex
	bibtex Elaborato.aux
	pdflatex Elaborato.tex
	pdflatex Elaborato.tex
ps: 
	latex Elaborato.tex && dvips -t a4 -Ppdf Elaborato.dvi

clean:
	rm *.toc *.out *.log *.aux
