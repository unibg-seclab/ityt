.PHONY: all run latex clean

SHELL          := /bin/bash
APP            := PoV.py
VENV           := venv
VIRTUALENV     := python3 -m venv --system-site-packages
ACTIVATE       := $(VENV)/bin/activate
PYTHON         := $(VENV)/bin/python
PIP            := $(PYTHON) -m pip

K = 15
N = 30
V = 1

all: run

$(VENV): $(ACTIVATE)

$(ACTIVATE): requirements.txt
	test -d $(VENV) || $(VIRTUALENV) $(VENV)
	$(PIP) install --quiet --upgrade pip
	$(PIP) install -r $<
	@ touch $(ACTIVATE)


run: $(VENV)
	$(PYTHON) $(APP) --k $(K) --n $(N) --v $(V) --format grid

latex: $(VENV)
	$(PYTHON) $(APP) --k $(K) --n $(N) --v $(V) --format latex

clean:
	@ rm -rf $(VENV)
	@ rm -rf build/ dist/ *.egg-info/
	@ find . -type d -name '__pycache__' -delete
	@ find . -type f -name '*.pyo' -delete
	@ find . -type f -name '*.pyc' -delete
