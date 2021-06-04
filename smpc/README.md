# Description

Here you can find the implementation of the secure Multi-Party
Comupation protocol described in the _ITYT_ framework.

The protocol comes in two flavors: 

* the _single-phase_ version, which permits to compute the shares and
  the commitments in a single round jointly executed by the Owner and
  the _n_ Shareholders;

* and the _two-phase_ version, which separates the production of
  shares and commitments in two steps (_step 1_ and _step 2_,
  respectively). _Step 1_ is jointly executed by the Owner and the _n_
  Shareholders, while _step 2_ is executed 1-to-1 by the Owner and
  each Shareholder.

## Prerequisites

If you are running Ubuntu 20.04, the only prerequisite is the project
management tool `maven`. You can install it by running on the console:

`sudo apt install maven`

## Compile and Run

We use _Make_ to compile and run the protocol. The following table
details details each target.

| target    | Description                                                |
|-----------|------------------------------------------------------------|
| `runSP`   | Simulates the _single-phase_ version of the protocol       |
| `runTPs1` | Simulates the _two-phase_ _step 1_ version of the protocol |
| `runTPs2` | Simulates the _two-phase_ _step 2_ version of the protocol |

To configure each simulation you have to edit the `Makefile`
accordingly. The following table details each variable.

| target    | Description                                                                             |
|-----------|-----------------------------------------------------------------------------------------|
| _N_       | The number of servers running the n-to-n protocol versions (number of shareholders + 1) |
| _K_       | The degree of the polynomial + 1                                                        |
| _ARGS_    | Input supplied to the mpc by one or manyparties (typically _n_, _k_)                    |
| _ARGS[i]_ | Input to the mpc supplied by the i-th Server                                            |

To remove the directories dedicated to each server you can run the
recipe `make clean`.

## Hints

Each Party is represented by a server. To each server a dedicated port
(starting from 8080) is associated. The application, logfiles, and
results related to each server are stored in the dedicated folder
`server/serverX`, where `X` stands for the identifier of the server
(`X=1` is always associated to the Owner). Please note that the
content under `server` can be wiped at each run (do not edit or create
the directories related to the server, they are automatically created
at runtime).
