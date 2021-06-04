pragma solidity ^0.5.6;
import "./MiMC.sol";

/*
ITYT-like contract
*/

/*
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
*/

contract ITYT_version0 {
  uint private creationTime = now;
  
  // valid states definition
  enum States {
    InTransition,
    Init, 
    Expired, 
    Initialized, 
    Pre_committed, 
    Pre_initialized, 
    Pre_verified, 
    TL_ended, 
    TL_started,  
    Failed, 
    Succeded 
  }
  States private state = States.Init;

  // participants
  address owner;      
  address[] shareholders;     
  // wallets
  mapping(address => uint) wallets;           

  // metadata
  bool[] bid_done;
  bool[] committed;     
  bool[] validated;     
  bool[] fastest;         

  // shares
  bytes32[] xs;
  bytes32[] ys;  

  // data from mpc
  bytes32[] xscoms;
  bytes32[] yscoms;         
  struct locked_data{ 
      uint256 sigmaxorsecret; 
      bytes32 sigma_com; }      
  locked_data ityt_data;           
  
  // time thresholds
  uint wait_window; uint withdraw_window; uint tl_window;      
  uint pi_window; uint i_window; uint pc_window; uint s_window;
  
  // econimic amounts (wei)
  uint16 n; uint16 k; uint256 beta; uint256 delta; uint256 Po; uint256 Ro; uint256 W; uint256 Bh; uint256 V;

  // counters
  uint bidders=0; uint committers=0; uint registered=0; 
  
function() external payable{
    
}


// function equivalent to <code>enforce</code>
function activateContract () public
{
    require(state == States.Pre_committed);
    //Guards
    require(n==committers);   
    //State change
    state = States.InTransition;
    //Actions
    s_window=now;   
    //State change
    state = States.TL_started; 
}

// function to reach the state FAILED when less than k shareholders have deposited their bids after the maximum wait time
function backfire () public
{
    require(state == States.Pre_verified);
    //Guards
    require(registered<k);     
    //State change
    state = States.Failed; 
}

// permits to close the time interval in which shareholders are allowed to disclose their share
function closeRegistration () public
{
    require(state == States.TL_ended);
    //Guards
    require(now > (s_window + tl_window + wait_window));     
    //State change
    state = States.Pre_verified; 
}

// function equivalent to <code>commit</code>
function commit () public
{
    require(state == States.Pre_committed);
    //State change
    state = States.InTransition;
    //Actions
    bool found=false;        
    uint pos=0;          
    for (uint i=0; i<n; i++) {                   
        if(msg.sender == shareholders[i]){                       
            found = true;                        
            pos = i;                 
        }                 
        
    }        
    if(!found || committed[pos])                 
        revert();        
    committers+=1;     
    committed[pos]=true;   
    //State change
    state = States.Pre_committed; 
}

// ends the TL and allows the shareholders to withdraw
function conclude () public
{
    require(state == States.Pre_verified);
    //Guards
    require(registered>=k);     
    //State change
    state = States.Succeded; 
}

// function equivalent to <code>initialize_sc</code>
function configureContract (uint256 _v, uint16 _n, uint16 _k, uint256 _beta, uint256 _delta, uint256 _Po, uint256 _Ro, uint256 _W, uint256 _Bh ,uint _wait_window, uint _withdraw_window, uint _tl_window, address[] memory _participants) public
 payable  
{
    require(state == States.Init);
    //State change
    state = States.InTransition;
    //Actions
    require(msg.sender.balance>=msg.value);      
    require(msg.value==_Po); 
    address(this).transfer(msg.value);
    owner=msg.sender; 
    wallets[msg.sender]+=msg.value;         
    require(_participants.length==_n);          
    wait_window=_wait_window;       
    withdraw_window=_withdraw_window;       
    tl_window=_tl_window;           
    pi_window=now;            
    V=_v; n=_n; k=_k; beta=_beta; delta=_delta; Po=_Po; Ro=_Ro; W=_W; Bh=_Bh;           
    committed = new bool[](n);
    validated = new bool[](n);
    bid_done = new bool[](n);
    fastest = new bool[](n);
    shareholders = new address[](n);
    xs = new bytes32[](n);
    ys = new bytes32[](n);    
    for (uint i=0; i<n; i++) {         
        committed[i]=false;         
        validated[i]=false;         
        bid_done[i]=false;         
        fastest[i]=false;         
        shareholders[i]=_participants[i];     
	xs[i]=0;
	xs[i]=0;	
    }   
    //State change
    state = States.Pre_initialized; 
}

// function equivalent to <code>finalizeMPC</code>
function contractFillIn (uint256[] memory _xscoms, uint256[] memory _yscoms, bytes32 _sigma_com) public
{
    require(state == States.Initialized);
    //Guards
    require(msg.sender==owner);
    //State change
    state = States.InTransition;
    //Actions
    require(_xscoms.length==n);
    require(_yscoms.length==n);          
    pc_window=now;     
    ityt_data.sigma_com=_sigma_com;     
    xscoms=new bytes32[](n);
    yscoms=new bytes32[](n);    
    for (uint i=0; i<n; i++) {          
        xscoms[i]=bytes32(_xscoms[i]);
        yscoms[i]=bytes32(_yscoms[i]);	
    }    
    //State change
    state = States.Pre_committed; 
}

// function equivalent to <code>participate</code>
function depositBid () public payable  
{
    require(state == States.Pre_initialized);
    //State change
    state = States.InTransition;
    //Actions
    require(msg.sender.balance>=msg.value);      
    require(msg.value==Bh);      
    bool found=false;        
    uint pos=0;          
    for (uint i=0; i<n; i++) {                   
        if(msg.sender == shareholders[i]){                       
            found = true;                        
            pos = i;                 
        }                 
    }        
    if(!found || bid_done[pos])                  
        revert();        
    require(msg.sender.balance>=msg.value); 
    address(this).transfer(msg.value);
    wallets[msg.sender]+=msg.value;          
    bidders+=1;     
    bid_done[pos]=true;   
    //State change
    state = States.Pre_initialized; 
}

// function equivalent to <code>terminate</code>
function endTLwindow () public
{
    require(state == States.TL_started);
    //Guards
    require(now > ( s_window + tl_window ));     
    //State change
    state = States.TL_ended; 
}

// to load the encrypted secret
function loadSecret (uint256 _sigmaxorsecret) public
{
    require(state == States.TL_started);
    //Guards
    require(msg.sender==owner);   
    //State change
    state = States.InTransition;
    //Actions
    ityt_data.sigmaxorsecret=_sigmaxorsecret;   
    //State change
    state = States.TL_started; 
}

// permits the shareholders to withdraw (setup failure 1)
function openBidWithdrawal1 () public
{
    require(state == States.Pre_initialized);
    //Guards
    require(now > (wait_window + pi_window));     
    //State change
    state = States.Expired; 
}

// permits the shareholders to withdraw (setup failure 2)
function openBidWithdrawal2 () public
{
    require(state == States.Initialized);
    //Guards
    require(now > (wait_window + i_window));     
    //State change
    state = States.Expired; 
}

// permits the shareholders to withdraw (setup failure 3)
function openBidWithdrawal3 () public
{
    require(state == States.Pre_committed);
    //Guards
    require(now > (wait_window + pc_window));     
    //State change
    state = States.Expired; 
}

// function equivalent to <code>startMPC</code>, permits to start the mpc
function openMPCwindow () public
{
    require(state == States.Pre_initialized);
    //Guards
    require(bidders == n);   
    //State change
    state = States.InTransition;
    //Actions
    i_window = now;   
    //State change
    state = States.Initialized; 
}

// function equivalent to <code>terminate</code>, also implementing the k-fastest bonus
function payback () public
{
    require(state == States.Succeded);
    //State change
    state = States.InTransition;
    //Actions
    bool found=false;        
    uint pos=0;          
    for (uint i=0; i<n; i++) {                   
        if(msg.sender == shareholders[i]){                       
            found = true;                        
        pos = i;                 
        }                 
    }        
    if(msg.sender!=owner && (!found || !validated[pos] || wallets[msg.sender]==0))
        revert(); 
    //avoid out-of-gas case re-entrancy
    //State remains the same  
    state = States.Succeded;     
    if(msg.sender!=owner){
        wallets[msg.sender]=0;   
        validated[pos]=false;         
        msg.sender.transfer(beta+Bh);  
        if(fastest[pos]){    
            fastest[pos]=false; 
            msg.sender.transfer(delta); 
        }
    }     
    else{         
        if(wallets[msg.sender]!=0){
	    wallets[msg.sender]=0;   
            msg.sender.transfer(Ro); 
        }
    }     

}
// function equivalent to <code>disclose</code>
function registerShare (uint256 _wbx, uint256 _wby, uint256 _salt) public
{
    require(state == States.TL_ended);
    //Guards
    require(now < (s_window + tl_window + wait_window));   
    //State change
    state = States.InTransition;
    //Actions
    bool found=false;        
    uint pos=0;          
    for (uint i=0; i<n; i++) {                   
        if(msg.sender == shareholders[i]){                       
            found = true;                        
            pos = i;                 
        }                
    }
    
    if(!found || !committed[pos])                 
        revert();
	
    //previously whistleblowed
    if(xs[pos]!=0)                 
        revert();

    //compute commitment with mimc
    // x
    bytes32 tmp = bytes32(MiMC.Encipher(_salt, _wbx));
    if(tmp!=xscoms[pos])             
        revert();      
    //store value
    xs[pos]=bytes32(_wbx);
  
    // y
    tmp = bytes32(MiMC.Encipher(_salt, _wby));
    if(tmp!=yscoms[pos])             
        revert();      
    //store value
    ys[pos]=bytes32(_wby);
    
    if(registered<k)         
        fastest[pos]=true;     
    registered+=1;     
    validated[pos]=true;   
    //State change
    state = States.TL_ended; 
}

// function equivalent to <code>WhistleblowShare</code>
function whistleblowshare (uint256 _wbx, uint256 _wby, uint256 _salt, uint pos) public
{
    require(state == States.TL_started);
    //Guards
    require(now < (s_window + tl_window) );   
    //State change
    state = States.InTransition;

    //check pos value
    if(pos>n || pos <0)
        revert();
    if(!committed[pos])                 
        revert();

    //recover the key of the scoopped shareholder
    address wblowed = shareholders[pos];
    
    //check if already paid
    if((xs[pos]!=0 || wallets[wblowed]==0))
        revert();

    //compute commitment with mimc
    // x
    bytes32 tmp = bytes32(MiMC.Encipher(_salt, _wbx));
    if(tmp!=xscoms[pos])             
        revert();      
    //store value
    xs[pos]=bytes32(_wbx);
    // y
    tmp = bytes32(MiMC.Encipher(_salt, _wby));
    if(tmp!=yscoms[pos])             
        revert();      
    //store value
    ys[pos]=bytes32(_wby);

    if(registered<k)         
        fastest[pos]=true;     
    registered+=1;     
    validated[pos]=true;
    
    //insert action to pay
    wallets[wblowed]=0;   
    validated[pos]=false;         
    msg.sender.transfer(uint256(Bh/2));  
}

// function equivalent to <code>WhistleblowSecret</code>
function whistleblow (uint256 _sigma, uint256 _salt) public
{
    require(state == States.TL_started);
    //Guards
    require(msg.sender!=owner);   
    //State change
    state = States.InTransition;
    //Actions
    bytes32 tmp = bytes32(MiMC.Encipher(_salt, _sigma));

    //compute commitment
    if(tmp==ityt_data.sigma_com){ 
        //State change
        state = States.Failed;
        uint val=W; 
        msg.sender.transfer(val); 
    }   

}

// permits the shareholders to withdraw (setup failure 4)
function withdraw () public
{
    require(state == States.Expired);
    //State change
    state = States.InTransition;
    //Actions
    require(wallets[msg.sender]>0); 
    uint val=wallets[msg.sender]; 
    wallets[msg.sender] = 0; 
    msg.sender.transfer(val);   
    //State change
    state = States.Expired; 
}


}//end contract
