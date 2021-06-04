package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.lang.Boolean;

public class TwoPhaseS2Result {

    private DRes<BigInteger> _Ck, _Cx, _Cy, _cheat;
    private BigInteger Ck, Cx, Cy;

    public TwoPhaseS2Result() {
    }

    public void set_Ck(DRes<BigInteger> _Ck) {
	this._Ck = _Ck;
    }

    public DRes<BigInteger> get_Ck() {
	return this._Ck;
    }

    public void set_Cx(DRes<BigInteger> _Cx) {
	this._Cx = _Cx;
    }

    public DRes<BigInteger> get_Cx() {
	return this._Cx;
    }

    public void set_Cy(DRes<BigInteger> _Cy) {
	this._Cy = _Cy;
    }

    public DRes<BigInteger> get_Cy() {
	return this._Cy;
    }

    public void set_cheat(DRes<BigInteger> _cheat) {
	this._cheat = _cheat;
    }

    public DRes<BigInteger> get_cheat() {
	return this._cheat;
    }

    public BigInteger getCk() {
	if (this.Ck == null) {
	    if (this._Ck == null) return null;
	    this.Ck = this._Ck.out();
	}
	return this.Ck;
    }

    public BigInteger getCx() {
	if (this.Cx == null) {
	    if (this._Cx == null) return null;
	    this.Cx = this._Cx.out();
	}
	return this.Cx;
    }
    
    public BigInteger getCy() {
	if (this.Cy == null) {
	    if (this._Cy == null) return null;
	    this.Cy = this._Cy.out();
	}
	return this.Cy;
    }
    
    public Boolean detectCheating(){
	return this.get_cheat().out().toString().equals("0") ? false : true;
    }

    @Override
    public String toString() {
	String cs = this.getCx() == null ? "null" : this.getCx() + "||" + this.getCy();
	return "TwoPhaseS2Result {" +
	    "\n Ck:\t" + this.getCk() +
	    "\n Cs:\t" + cs +
	    "\n}";
    }
}
