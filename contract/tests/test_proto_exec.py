#!/usr/bin/python3

import sys

from brownie import *

import scripts.ityt_t0
import csv
import numpy as np

def test_setup():
    # preprocessing and setup
    scripts.ityt_t0.initial_money_transfer()
    # read example values
    scripts.ityt_t0.read_shares()
    scripts.ityt_t0.read_secrets()

    k_min=2
    k_max=2
    n_min=2
    n_max=10

    # exec (n,k) instance
    # WARNING be sure that participants' wallets have enough currency to run tests
    
    for n in range(n_min, n_max+1):
        for k in range(k_min, min(n, k_max)+1):
            scripts.ityt_t0.main(n, k)
