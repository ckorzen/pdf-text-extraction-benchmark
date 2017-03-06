checkstyle:
	@flake8 $(shell find . -type f -name "*.py" -not -wholename "./parse/tex_tokenizer.py")

test:
	for T in $(shell find . -type f -name "test_*.py"); do python3 $$T; done

clean:
	@rm -rf $(shell find . -type d -name "__pycache__")

