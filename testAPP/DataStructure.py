class Extra:
    def __init__(self, key, type, value):
        self.key = key
        self.type = type
        self.value = value

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Extra)):
            if (self.key == o.key and self.type == o.type and self.value == o.value):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * (31 * hash(self.key) + hash(self.type)) + hash(self.value)

    def __str__(self) -> str:
        return self.type + "&" + self.key + "&" + str(self.value)


class Extra_Key:
    def __init__(self, key, type, value):
        self.key = key
        self.type = type
        self.value = value

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Extra)):
            if (self.key == o.key and self.type == o.type):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * (31 * hash(self.key) + hash(self.type))

    def __str__(self) -> str:
        return self.type + "&" + self.key

class IntentData:
    def __init__(self, dataString,type) -> None:
        self.dataString = dataString
        self.type = type

    def __str__(self) -> str:
        str=''
        if(self.dataString==None):
            str=str+""
        else:
            str=str+self.dataString

        if(self.type==None):
            str=str+";"+""
        else:
            str=str+";"+type

        return str

    def __eq__(self, o: object) -> bool:
        if(isinstance(o, IntentData)):
            if(self.dataString==o.dataString and self.type==o.type):
                return True
        return False

    def __hash__(self) -> int:
        return 31*hash(self.dataString)+hash(self.type)


class Intent:
    def __init__(self, appPath, appPackageName, comPonentType, comPonentName, action, categorySet, comPonentData,
                 extraSet) -> None:
        self.appPath = appPath
        self.appPackageName = appPackageName
        self.comPonentType = comPonentType
        self.comPonentName = comPonentName
        self.action = action
        self.categorySet = categorySet
        self.comPonentData = comPonentData
        self.extraSet = extraSet

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Intent)):
            if (self.getBaseStr(self) == self.getBaseStr(o) and self.getActionStr(
                    self) == self.getActionStr(
                o)):
                if (self.getCategorySet(self) == self.getCategorySet(o) and self.getDataStr(
                        self) == self.getDataStr(
                    o)):
                    if (self.getExtraSet(self) == self.getExtraSet(o)):
                        return True

        return False

    def getBaseStr(self, intent):
        line = ''
        if (intent.appPath != None):
            line = line + intent.appPath + "#"
        else:
            line = line + "null" + "#"
        if (intent.appPackageName != None):
            line = line + intent.appPackageName + "#"
        else:
            line = line + "null" + "#"
        if (intent.comPonentType != None):
            line = line + intent.comPonentType + "#"
        else:
            line = line + "null" + "#"
        if (intent.comPonentName != None):
            line = line + intent.comPonentName + "#"
        else:
            line = line + "null" + "#"

        return line

    def getActionStr(self, intent):
        line = ''
        if (intent.action != None):
            line = line + intent.action + "#"
        else:
            line = line + "null" + "#"
        return line

    def getCategorySet(self, intent):
        categorySet = set()
        if (intent.categorySet == None or len(intent.categorySet) == 0):
            categorySet.add("null")
        else:
            for category in intent.categorySet:
                categorySet.add(category)
        return categorySet

    def getDataStr(self, intent):
        line = ''
        if (intent.comPonentData != None):
            line = line + intent.comPonentData.__str__() + "#"
        else:
            line = line + "null" + "#"
        return line

    def getExtraSet(self, intent):
        extraSet = set()
        if (intent.extraSet == None or len(intent.extraSet) == 0):
            extraSet.add("null")
        else:
            for extra in intent.extraSet:
                extraStr = extra.type + "&" + extra.key + "&" + str(extra.value)
                extraSet.add(extraStr)
        return extraSet

    def __hash__(self) -> int:
        return hash(self.__str__())

    def __str__(self) -> str:
        line = ''
        if (self.appPath != None):
            line = line + self.appPath + "#"
        else:
            line = line + "null" + "#"
        if (self.appPackageName != None):
            line = line + self.appPackageName + "#"
        else:
            line = line + "null" + "#"
        if (self.comPonentType != None):
            line = line + self.comPonentType + "#"
        else:
            line = line + "null" + "#"
        if (self.comPonentName != None):
            line = line + self.comPonentName + "#"
        else:
            line = line + "null" + "#"
        if (self.action != None):
            line = line + self.action + "#"
        else:
            line = line + "null" + "#"
        categoryStr = ''
        if (self.categorySet == None or len(self.categorySet) == 0):
            categoryStr = "null"
        else:
            for category in self.categorySet:
                if (categoryStr == ''):
                    categoryStr = category
                else:
                    categoryStr = categoryStr + "," + category
        line = line + categoryStr + "#"

        if (self.comPonentData != None):
            line = line + self.comPonentData.__str__() + "#"
        else:
            line = line + "null" + "#"

        extraStr = ''
        if (self.extraSet == None or len(self.extraSet) == 0):
            extraStr = "null"
        else:
            for extra in self.extraSet:
                if (extraStr == ""):
                    extraStr = extra.type + "&" + extra.key + "&" + str(extra.value);
                else:
                    extraStr = extraStr + ";" + extra.type + "&" + extra.key + "&" + str(extra.value);
        line = line + extraStr;
        return line