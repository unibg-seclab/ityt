package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TwoPhaseS1Result {

    private List<DRes<BigInteger>> secretCoefficients, secretYs, secretXs;
    private List<BigInteger> coefficients, ys, xs;

    public TwoPhaseS1Result() {}

    public void setSecretYs(List<DRes<BigInteger>> secretYs) {
	this.secretYs = secretYs;
    }

    public void setSecretXs(List<DRes<BigInteger>> secretXs) {
	this.secretXs = secretXs;
    }

    public void setSecretCoefficients(List<DRes<BigInteger>> secretCoefficients) {
	this.secretCoefficients = secretCoefficients;
    }

    public List<BigInteger> getYs() {
	if (this.ys == null) {
	    if (this.secretYs == null) return null;
	    this.ys = this.secretYs.stream().map(DRes::out)
		.collect(Collectors.toList());
	}
	return this.ys;
    }

    public List<BigInteger> getXs() {
	if (this.xs == null) {
	    if (this.secretXs == null) return null;
	    this.xs = this.secretXs.stream().map(DRes::out)
		.collect(Collectors.toList());
	}
	return this.xs;
    }

    public List<BigInteger> getCoefficients() {
	if (this.coefficients == null) {
	    if (this.secretCoefficients == null) return null;
	    this.coefficients = this.secretCoefficients.stream().map(DRes::out)
		.collect(Collectors.toList());
	}
	return this.coefficients;
    }

    @Override
    public String toString() {
	return "TwoPhaseS1Result {" +
	    "\n Coeffs:" + this.getCoefficients() +
	    "\n Xs:\t" + this.getXs() +	    
	    "\n Ys:\t" + this.getYs() +
	    "\n}";
    }

}
