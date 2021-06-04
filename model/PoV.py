#!/usr/bin/env python3

import argparse
import itertools

import tabulate
import z3


def optimize(k, n, v):
    print("\n[*] k = %d; n = %d; V = %d" % (k, n, v))

    # Create variables
    K, N = z3.Ints("K N")
    V, Fo, Bh, Rh, Ws, Wh = z3.Reals("V Fo Bh Rh Ws Wh")
    model = z3.Optimize()

    # Fix variables
    model.add(K == k)
    model.add(N == n)
    model.add(V == v)

    # All positive
    for var in [K, N, V, Fo, Bh, Rh, Ws, Wh]:
        model.add(var > 0)

    # Add model constraints
    model.add(Rh > Bh)                                 # (Eq 1)
    model.add(Wh < Bh)                                 # (Eq 1)
    model.add(Ws > Rh)                                 # (Eq 1)
    model.add(K * Bh > V + Ws)                         # (Eq 2)
    model.add((N - K + 1) * Rh > V)                    # (Eq 3)
    model.add(Fo > Ws)                                 # (Eq 4)
    model.add(K * Bh > V + Ws + (2 * K - N - 1) * Wh)  # (Eq 5)
    model.add(Fo + n * Bh > n * Rh)                    # (Eq 6)
    model.add(Fo + n * Bh > Ws + (k - 1) * Wh)         # (Eq 7)
    model.add(Fo < V)                                  # (Eq 8)

    # Minimize Fo
    # model.minimize(Fo)
    # While maximizing the revenue
    model.maximize(Rh - Bh - Fo)

    # Check if model is sat and return
    check = model.check()
    #print(check)
    #print("\n")
    if check != z3.sat:
        return [k, n, v] + [None] * 7

    X = model.model()
    #print(X)
    #print("\n")

    def fp(y):
        Y = X[y]
        return float(Y.numerator_as_long()) / float(Y.denominator_as_long())

    return [k, n, fp(V), fp(Wh), fp(Bh), fp(Rh), fp(Ws), fp(Fo),
            fp(Fo) / fp(V), fp(Rh) / fp(Bh)]


def main():
    parser = argparse.ArgumentParser(
        description="Get the ITYT economic parameters based on n, k, and V.")
    parser.add_argument("--n", type=int, nargs="+", default=[8],
                        required=False, help="# of shareholders.")
    parser.add_argument("--k", type=int, nargs="+", default=[5],
                        required=False, help="Required shareholders.")
    parser.add_argument("--v", type=int, nargs="+", default=[1, 10, 100],
                        required=False, help="Value of the secret.")
    parser.add_argument("--format", type=str, default="grid", required=False,
                        help="Table format (e.g., grid, latex, simple, ...)")
    args = parser.parse_args()
    tabulate.tabulate

    headers = ["k", "n", "V", "Wh", "Bh", "Rh", "Ws", "Fo", "Fo/V", "Rh/Bh"]
    results = [optimize(k=k, n=n, v=v)
               for (k, n, v) in itertools.product(args.k, args.n, args.v)
               if n >= k]

    print("\n[*] RESULTS\n")
    print(tabulate.tabulate(results,
                            headers=headers,
                            floatfmt=".4f",
                            tablefmt=args.format))


if __name__ == "__main__":
    main()
