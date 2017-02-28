PYTHON = @python3

EBNF = tex.ebnf
OUT = tex_tokenizer.py

tokenizer: $(EBNF)
	$(PYTHON) -m grako -o $(OUT) $(EBNF)

