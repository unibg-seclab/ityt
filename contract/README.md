# Description

Here you can find a smart contract compatible with the _ITYT_
framework.

## Prerequisites

This implementation relies on MiMC to produce the commitments.  To
compile the smart contract with solidity, a library implementing the
MiMC primitive needs to be included in the directory `contracts` as a
file named `MiMC.sol`.  An example of such library implementation can
be found
[here](https://gist.github.com/HarryR/80b5ff2ce13da12edafda6d21c780730#file-mimcp-sol).

## Install

The following procedure has been tested on Ubuntu 20.04.

1. Install _solc_: 

```bash
sudo snap install solc
```

2. Install _ganache-cli_: 

```bash
sudo apt install nodejs
sudo apt install npm
sudo npm install -g ganache-cli
```

3. Install Python dependencies

```bash
# Create and activate a virtualenv
python3 -m venv venv
source venv/bin/activate

# Install requirements
pip install -r requirements

# Install Solcx required version

python -c "import solcx; solcx.install_solc('v0.5.6')"
```

## Test

To deploy, test, and debug the contract generated, we rely on
[Brownie](https://eth-brownie.readthedocs.io/en/stable/), a Python
framework that allows to create wallets, inspect transactions and
automatize tests.

Running `brownie test` the script `test_proto_exec.py` is executed.
Based on the (_n_, _k_) values specified, the script runs multiple
smart contract simulations. Use the `-G` option to display the gas
profile for function calls.
