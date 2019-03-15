package com.popoaichuiniu.intentGen;



import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IntentInfo {


        String appPath;
        String appPackageName;
        String comPonentType;
        String comPonentName;

        String comPonentAction;
        List<String> comPonentCategory=new ArrayList<>();
        String comPonentData;
        List<IntentExtraKey> comPonentExtraData=new ArrayList<>();

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                IntentInfo that = (IntentInfo) o;
                return Objects.equals(appPath, that.appPath) &&
                        Objects.equals(comPonentType, that.comPonentType) &&
                        Objects.equals(comPonentName, that.comPonentName) &&
                        Objects.equals(comPonentAction, that.comPonentAction) &&
                        Objects.equals(comPonentCategory, that.comPonentCategory) &&
                        Objects.equals(comPonentData, that.comPonentData) &&
                        Objects.equals(comPonentExtraData, that.comPonentExtraData);
        }

        @Override
        public int hashCode() {
                return Objects.hash(appPath, comPonentType, comPonentName, comPonentAction, comPonentCategory, comPonentData, comPonentExtraData);
        }
}
