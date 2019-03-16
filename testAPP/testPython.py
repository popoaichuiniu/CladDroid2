def add(list,index,set1,allSet):
    if index>=len(list):
        allSet.add(frozenset(set1))
    else:
        setCopy=set(set1)
        ele=list[index]
        add(list,index+1,setCopy,allSet)
        setCopy.add(ele)
        add(list, index + 1, setCopy, allSet)
list=[1,2]
index=0
allSet=set()
add(list,index,set(),allSet)
print(allSet)