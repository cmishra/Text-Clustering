library(data.table)
library(ROCR)
chartOutput2 <- data.table (
  read.csv("../CS 6501/MP2/MP2/data/chartOutput.txt", sep=",", header=F))
# suggie version
chartOutput <- data.table(read.csv("C:/Users/cheta_000/Downloads/featureScore.csv",
  header=F))
chartOutput[, V1:=-1*V1]
setkey(chartOutput, V1)
# chartOutput[order(-1*V1)]

chartOutput[,V2:=as.integer(V2)-1]
chartOutput[,pred:=ifelse(V1<0, 0, 1)]

# suggie version: 
chartOutput[,V3:=as.factor(V3)]

chart.pred <- with(chartOutput, prediction(V2, V3))
chart.perf <- performance(chart.pred, measure="prec","rec")
plot(chart.perf, ylim=c(0,1), main="Precision Recall of Naive Bayes", xaxt="n", col="blue")+
  axis(1, at=seq(0, 1.0, by=0.1)) +
  axis(2, at=seq(0, 1.0, by=0.1))


data(ROCR.simple)
pred <- prediction( ROCR.simple$predictions, ROCR.simple$labels)
perf <- performance(pred,"prec","rec")
plot(perf)


cvResults <- data.table(read.csv("../CS 6501/MP2/awsOutputs/CVresults10_5.txt", 
  header=T, row.names=NULL, col.names=c("IterationNum", "ModelType",
    "TP", "TN", "FP", "FN")))
)# names(cvResults)[1:6] <- names(cvResults)[2:7]
# cvResults[,7:=NULL]
cvResults[,Prec:=TP/(TP+FP)]
cvResults[,Rec:=TP/(TP+FN)]
cvResults[,F1:=2/(1/Prec+1/Rec)]
write.csv(cvResults, file="../CS 6501/MP2/Task4Results.csv")
t.test(x=cvResults[ModelType=="KNN", F1], 
  y=cvResults[ModelType=="Naive Bayes", F1],
  alternative="two.sided",
  paired=T)


getKnnF1s <- function(filename) {
  cvResults <- data.table(read.csv(filename, 
    header=T, row.names=NULL, col.names=c("IterationNum", "ModelType",
      "TP", "TN", "FP", "FN")))[ModelType=="KNN"]
  cvResults[,Prec:=TP/(TP+FP)]
  cvResults[,Rec:=TP/(TP+FN)]
  cvResults[,F1:=2/(1/Prec+1/Rec)]
  cvResults[,F1]
}

KNN10_5 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults10_5.txt")
KNN10_6 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults10_6.txt")
KNN10_4 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults10_4.txt")
KNN9_5 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults9_5.txt")
KNN11_5 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults11_5.txt")

t.test(KNN10_5, KNN10_6, paired=T) # about the same
t.test(KNN10_5, KNN10_4, paired=T) # 10_4 significantly worse
t.test(KNN10_5, KNN9_5, paired=T)  # 9_5 significantly better 
t.test(KNN10_5, KNN11_5, paired=T) # nearly the same 
write.csv(data.table(KNN10_5, KNN10_4, KNN10_6, KNN9_5, KNN11_5), 
  file = "../CS 6501/MP2/ParameterSearchIter1.csv")
# new baseline is 9, 5

KNN9_4 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults9_4.txt")
KNN9_6 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults9_6.txt")
KNN8_5 <- getKnnF1s("../CS 6501/MP2/awsOutputs/CVresults8_5.txt")
write.csv(data.table(KNN9_5, KNN9_4, KNN9_6, KNN8_5), 
  file = "../CS 6501/MP2/ParameterSearchIter2.csv")

t.test(KNN9_5, KNN9_4, paired=T)
t.test(KNN9_5, KNN9_6, paired=T)
t.test(KNN9_5, KNN8_5, paired=T)


