package ljy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class Two implements Writable {

    private long loanAmnt; //贷款金额
    private long annualIncome; // 年收入
    
    public Two() {
    	
    }
    public Two(long loanAmnt, long annualIncome) {
    	this.loanAmnt = loanAmnt;
        this.annualIncome = annualIncome;
    }
    
	public long getLoanAmnt() {
		return loanAmnt;
	}
	public void setLoanAmnt(long loanAmnt) {
		this.loanAmnt = loanAmnt;
	}
	public long getAnnualIncome() {
		return annualIncome;
	}
	public void setAnnualIncome(long annualIncome) {
		this.annualIncome = annualIncome;
	}
	@Override
	 public void readFields(DataInput dataInput) throws IOException {
        this.loanAmnt = dataInput.readLong();
        this.annualIncome = dataInput.readLong();
    }
	@Override
	 public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(loanAmnt);
        dataOutput.writeLong(annualIncome);
    }

	public void set(long loanAmnt, long annualIncome) {
		 this.loanAmnt = loanAmnt;
	     this.annualIncome = annualIncome;
	}
	@Override
	    public String toString() {
	        return loanAmnt + "," + annualIncome;
	    }
}
