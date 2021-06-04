# Description

`PoV.py` is a Python script that can be used to find a solution to the
_ITYT_ Economic Model. To this end, the script leverages Z3, a theorem
prover from Microsoft Research.

To run it you just need to edit the `Makefile` setting the values
associated to _V_, _n_ and _k_, and then run the `make`.

In the following you can find a list of mappings that bind each script
variable to the related economic amount, as described in the paper.

| Script variable | Economic Amount | Description                                         |
|-----------------|-----------------|-----------------------------------------------------|
| _V_             | _V_             | economic value assigned to the secret               |
| _N_             | _n_             | number of shareholders                              |
| _K_             | _k_             | number or shares required to reconstruct the secret |
| _Rh_            | _Rh_            | shareholder reward                                  |
| _Bh_            | _Bh_            | shareholder bid                                     |
| _Fo_            | _Fo_            | fee paid by the owner to get the service            |
| _Ws_            | _Ws_            | reward paid when whistleblowing the secret          |
| _Wh_            | _Wh_            | reward paid when whistleblowing a share             |
