PYTHON = @python3
CHECKSTYLE = @flake8

EBNF = tex.ebnf
OUT = tex_tokenizer.py

tokenizer: $(EBNF)
	$(PYTHON) -m grako -o $(OUT) $(EBNF)

checkstyle:
	$(CHECKSTYLE) $(shell find . -type f -name '*.py' -not -name $(OUT))

clean:
	@rm -f $(OUT)

