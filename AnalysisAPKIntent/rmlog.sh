#!/bin/sh
#dir_name=$1
#mkdir $dir_name
#cp -r intent_file $dir_name
#cp -r intent_ulti $dir_name
#cp -r intentPass $dir_name
#cp testFunctions/isDefInPathAndLatest.txt $dir_name
#cp testFunctions/isFallThrough.txt $dir_name
#cp intentConditionSymbolicExcutationResults/appException.txt $dir_name
#cp intentConditionSymbolicExcutationResults/callGraphSize.txt $dir_name
#cp intentConditionSymbolicExcutationResults/if_reduced.txt $dir_name
#cp intentConditionSymbolicExcutationResults/RepeatEdgeSituation.txt $dir_name
#cp intentConditionSymbolicExcutationResults/unitGraphSize.txt $dir_name
#cp intentConditionSymbolicExcutationResults/EAToTargetAPIPPAthCount.txt $dir_name
#cp intentConditionSymbolicExcutationResults/ifeasiblePath.txt $dir_name
#cp intentConditionSymbolicExcutationResults/appUnitGraphReachLimit.txt $dir_name
#cp intentConditionSymbolicExcutationResults/unhandleSituation.txt $dir_name
#cp intentConditionSymbolicExcutationResults/errorSymbolicExcuation.txt $dir_name
#cp intentConditionSymbolicExcutationResults/sootmethodEdgeNoSrcUnit.txt $dir_name
#cp intentConditionSymbolicExcutationResults/joinIntentResults.txt $dir_name
#cp intentConditionSymbolicExcutationResults/if.txt $dir_name
#cp intentConditionSymbolicExcutationResults/timeUse.txt $dir_name
#cp intentConditionSymbolicExcutationResults/callgraphLimit.txt $dir_name
#
log_dir=/home/zms/logger_file
rm ${log_dir}"/intent_file/"*txt
rm ${log_dir}"/intent_ulti/"*txt
rm ${log_dir}"/intentPass/"*txt
rm testFunctions/isDefInPathAndLatest.txt
rm testFunctions/isFallThrough.txt

rm ${log_dir}"/intentConditionSymbolicExcutationResults/appException.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/callGraphSize.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/if_reduced.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/RepeatEdgeSituation.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/unitGraphSize.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/EAToTargetAPIPPAthCount.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/ifeasiblePath.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/appUnitGraphReachLimit.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/unhandleSituation.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/errorSymbolicExcuation.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/sootmethodEdgeNoSrcUnit.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/joinIntentResults.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/if.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/timeUse.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/callgraphLimit.txt"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/exceptionLogger.log"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/info.log"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/callgraphLimitRepeat.txt"
rm ${log_dir}"/com.popoaichuiniu.intentGen.IntentConditionTransformSymbolicExcutation.log"
rm ${log_dir}"/intentConditionSymbolicExcutationResults/UnHandleWriter/"*