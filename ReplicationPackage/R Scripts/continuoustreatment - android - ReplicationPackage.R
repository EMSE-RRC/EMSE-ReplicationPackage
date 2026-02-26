library (causaldrf)



ElapsedTime <- as.numeric(x$elapsed.time)
Interest <- as.numeric(x$elapsed.comments)
Contributors <- as.numeric(x$commenters)
CommunityInterest <- as.numeric(x$comments.by.nonowners)
ExternalContributors <- as.numeric(x$commenters.nonowners)
DeveloperExperience <- as.numeric(x$owner.bugs)
OwnerEngagement <- as.numeric(x$owner.comments)
OwnerSpectrum <- as.numeric(x$owner.comments.nonowned)
TotalVertices <- as.numeric(x$total.vertices)
NewInteractions <- as.numeric(x$new.new)
FreshInteractions <- as.numeric(x$new.inc)
SeasonedInteractions <- sqrt(as.numeric(x$inc.inc))#Rooted
RepeatInteractions <- (sqrt(as.numeric(x$repeat.edges)))#Rooted
TotalDistinctEdges <- as.numeric(x$total.distinct.edges)
TotalEdges <- as.numeric(x$total.edges)

#TeamSize=as.numeric(x$team.size)


##    Pick Treatment 
treatment_string <- "RepeatInteractions"
##    Pick Treatment




treatment <- get(treatment_string)

xval <- seq(min(treatment), max(treatment), by = (max(treatment)-min(treatment))/100)

mydata<-cbind(Contributors,
              CommunityInterest,
              DeveloperExperience,
              OwnerEngagement,
              OwnerSpectrum,
              NewInteractions,
              FreshInteractions,
              SeasonedInteractions,
              RepeatInteractions,
              
              
              treatment,
              ElapsedTime)
mydata<-data.frame(mydata)

myvars = names(mydata)
myvars = myvars[myvars!="treatment"]
myvars = myvars[myvars!="ElapsedTime"]
myvars = myvars[myvars!=treatment_string]

form<-paste("treatment ~ ",paste(myvars, collapse="+ "))

iptw_estimate <- iptw_est(Y = ElapsedTime,
                          treat = treatment,
                          treat_formula = as.formula(form),
                          numerator_formula = treatment ~ 1,
                          data = mydata, degree = 2,
                          treat_mod = "Normal")

yval <- iptw_estimate$param[1] + iptw_estimate$param[2]*xval + iptw_estimate$param[3]*xval*xval






