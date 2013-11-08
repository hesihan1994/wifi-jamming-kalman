package com.none.detectwifijammer;

public class WifiKalmanFilter{
	private	int distributionCount;
	private	float distributionMean;
	private	float distributionSumSquare;
	private	float distributionVariance;
	
	private	float kalmanQ;
	private	float kalmanGain;
	private	float currentEstimate;
	private	float currentCovariance;
	
	private	float nextEstimate;
	private	float nextCovariance;
	
	public void clearFilter(){
		kalmanQ = 0;
		kalmanGain = 1;
		currentEstimate = 0;
		currentCovariance = 1;
		
		distributionCount = 0;
		distributionMean = 0;
		distributionSumSquare =0;
		distributionVariance =0;
	}
	
	public void initialiseFilter(float setQ, float initialValue){
		kalmanQ = setQ;
		distributionCount = 1;
		distributionMean = initialValue;
		currentEstimate = initialValue;
		estimateNext();
	}
	
	public float updateFilter(float currentValue) {
		estimateMean(currentValue);
		kalmanPredict(currentValue);
		estimateNext();
		return currentEstimate;
	}
	
	private void estimateMean(float currentValue) {
		float tempMean = distributionMean;
		distributionCount++;
		distributionMean += (currentValue - tempMean)*distributionCount;
		distributionSumSquare += ((currentValue - tempMean)* (currentValue - distributionMean));
		distributionVariance = distributionSumSquare/ (distributionCount -1);
	}
	
	private void kalmanPredict(float currentValue){
		kalmanGain = nextCovariance/(nextCovariance+ distributionVariance);
		currentEstimate = nextEstimate + kalmanGain* (currentValue - nextEstimate);
		currentCovariance = (1-kalmanGain)* nextCovariance;
	}
	
	private void estimateNext(){
		nextEstimate = currentEstimate;
		nextCovariance = currentCovariance+ kalmanQ;
	}
}
