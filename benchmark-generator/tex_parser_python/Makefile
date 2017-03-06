checkstyle:
	@flake8 $(shell find . -type f -name '*.py' -not -wholename "./parse/tex_tokenizer.py")

test:
	python3 $(shell find . -type f -name '*_test.py')

clean:
	@rm -rf $(shell find . -type d -name "__pycache__")

