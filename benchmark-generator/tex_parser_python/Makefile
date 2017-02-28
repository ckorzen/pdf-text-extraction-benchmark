checkstyle:
	@flake8 $(shell find . -type f -name '*.py' -not -wholename "./parser/tex_tokenizer.py")

clean:
	@rm -rf $(shell find . -type d -name "__pycache__")

