#!/usr/bin/python3

import sys
import os
from brownie import *
import csv

'''
This script simulates an ITYT protocol instance given (n,k) using Brownie.
Current configuration
n -> [1 to 10]
k -> [2 to n]

Naming Conventions

script       -> paper

secret_value -> V         economic value assigned to the secret
n            -> n         number of shareholders
k            -> k         number or shares needed to reconstruct the secret
Bh           -> Bh        shareholder bid
Po           -> Fo        fee paid by the owner to get the service
Ro           -> missing   economic amount given back to the owner if the TL ends successfully
beta         -> missing   Rh - Bh (shareholders profit)
delta        -> missing   additional rewark paid to the k fastest-to-submit shareholders
W            -> Ws        reward paid when whistleblowing the secret
Bh/2         -> Wh        reward paid when whistleblowing a share
The parameters Ro, beta and delta are added as an extension to the current model
'''


# variables to model external input (mpc's output)
# xy_sh_and_shci = []
shares_and_coms = []
sigma_ci = 0
sigma_salt = 0
wrapped_secret = 0

# simple util to transfer funds from initial wallets
def initial_money_transfer():
    # ether transfer
    for x in range(0,10):
        accounts[x].transfer(accounts[0], "8 ether")

# read the shares and the commitments
def read_shares():
    sys.stdout.write("[i] current working directory: "+os.getcwd()+"\n")
    with open('./scripts/input_shares.csv') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        tacc = 0
        for row in csv_reader:
            if (tacc > 0):
                shares_and_coms.append([row[0], row[1], row[2], row[3], row[4] ])
            tacc+=1

# read the commitment of the key and the encrypted secret
def read_secrets():
    with open('./scripts/input_secret.csv') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        tacc = 0
        for row in csv_reader:
            if (tacc > 0):
                sigma_ci = row[0]
                sigma_salt = row[1]                
                wrapped_secret = row[2]
            tacc+=1

# util to create the account of the users
def accounts_generation(n, k, owner, shareholders, mimc_contract):
    # send ether to all the users
    accounts[0].transfer(owner, "5 ether")
    for x in range(0, n):
        shareholders.append(accounts.add())
        accounts[0].transfer(shareholders[x], "2 ether")

    # deployment of mimc contract
    mimc_contract = accounts[0].deploy(MiMC)

    # print balances
    sys.stdout.write("\n-----TEST("+str(n)+","+str(k)+")-----\n")

# run the (n,k) test
def main(n, k):
    
    # sett up all accounts
    owner = accounts.add()
    shareholders = []
    # fake temp value
    mimc_contract = 1

    # generate accounts
    accounts_generation(n, k, owner, shareholders, mimc_contract)
    sys.stdout.write("[*] Users' account created\n")

    # contract deployment
    ityt = owner.deploy(ITYT_version0)
    owner_sbmd = owner.balance()
    
    sys.stdout.write("[*] Contract deployed\n")

    # parameterization (in the following time is measured using seconds)
    
    wait_window=20
    withdraw_window=25
    tl_window=100
 
    addresses=[]
    for x in range(0, n):
        addresses.append(shareholders[x])
    sys.stdout.write("...Shareholder addresses: " + str(addresses) + "\n")
    
    secret_value=Wei("3 ether")
    beta=Wei("0.3 ether")/n
    delta=Wei("0.15 ether")/k
    po="1.8 ether"
    ro="1.2 ether"
    w=Wei("1 ether")/n
    bh=Wei("0.8 ether")

    # contract configuration
    tx=ityt.configureContract(
        secret_value, n,k,beta,delta,po,ro,w,bh,
        wait_window,withdraw_window,tl_window,addresses,
        {'from': owner, 'value': po}
    )
    
    # depositing bids
    sys.stdout.write("[*] Depositing bids\n")
    for x in range(0, n):
        tx=ityt.depositBid({'from':shareholders[x], 'value':bh})

    # opening mpc window
    sys.stdout.write("[*] Opening MPC window\n")
    tx4=ityt.openMPCwindow({'from':owner})
    
    # configure contract
    sys.stdout.write("[*] Contract configuration\n")
    # share subset selection
    xscoms = []
    yscoms = []
    for x in range(0, n):
        xscoms.append(shares_and_coms[x][2])
        yscoms.append(shares_and_coms[x][3])        

    tx5=ityt.contractFillIn(xscoms, yscoms, sigma_ci, {'from':owner})

    # commit
    sys.stdout.write("[*] Shareholders commit\n")
    for x in range(0, n):
        tx6=ityt.commit({'from':shareholders[x]})

    # activation
    sys.stdout.write("[*] Contract activation\n")
    tx9=ityt.activateContract({'from':owner})

    # load secret
    sys.stdout.write("[*] Loading secret\n")
    tx10=ityt.loadSecret(wrapped_secret,{'from':owner})

    # TL end
    chain.sleep(tl_window+1)
    sys.stdout.write("[*] TL end\n")
    tx11=ityt.endTLwindow({'from':shareholders[0]})

    # shares registration
    sys.stdout.write("[*] Shares registration\n")
    for x in range(0, n):
        tx12=ityt.registerShare(shares_and_coms[x][0], shares_and_coms[x][1], shares_and_coms[x][4], {'from':shareholders[x]})

    # close registration
    chain.sleep(wait_window+1)
    sys.stdout.write("[*] Closing registration\n")
    tx15=ityt.closeRegistration({'from':shareholders[0]})

    # ending protocol
    sys.stdout.write("[*] Ending protocol\n")
    tx16=ityt.conclude({'from':shareholders[0]})
    
    # shareholders reward
    sys.stdout.write("[*] Rewarding shareholders\n")
    tx17=ityt.payback({'from':owner})
    for x in range(0, n):
        tx18=ityt.payback({'from':shareholders[x]})

    sys.stdout.write("---------------\n")

    return
