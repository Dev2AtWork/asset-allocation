#Asset Allocation Problem
System to allocate user funds based on the Deposit Plans chosen by the customer and automatically distributing funds based on the customer's deposits.
##Core Idea
Every user will have 2 plans associated with user portfolio - 
1. High Risk Plan
2. Retirement Plan

Every user plan will have 2 types of investment amounts defined
1. One time investment
2. Monthly investment

Each user will have a Risk Appetite associated to the profile (on a scale of 0-1). Based on the risk appetite the user is categorised into 3 types - 
1. Aggressive
2. Balanced
3. Defensive

###Fund Allocation Strategy
* Allocate funds to each category till the one time investment goal is met.
    * The fund distribution strategy is derived by customer's Risk Appetite. (_Aggresive portfolio will prioritize achieving the high risk plan goal first, whereas Defensive portfolio will target to fulfill Retirement goal first_)
      
All use cases are written under `/test/resources/acceptance-criteria.yml`.

####Example
```yaml
- depositPlan:
    onetime:
      highRisk: 10000
      retirement: 500
    monthly:
      highRisk: 0
      retirement: 100
    riskAppetite: 0.2
  description: Happy Path, Defensive
  deposits:
    - 10500
    - 100
  fundAllocation:
    highRisk: 10000
    retirement: 600
```
The above is an example of a scenario, where user chooses a deposit plan with specified High Risk & Retirement plan fund allocation. `deposits` is a list of monthly deposits of the customer. `fundAllocation` is the final allocation expected after all the deposits.
