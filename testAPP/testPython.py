import random
abc=['A','B','C','D','E']
numInt=[1,0,-1]
numFloat = [0.1, 0, -0.1]
extraValuestr= ''
for i in range(random.randint(4,10)):
    extraValuestr= extraValuestr + str(abc[random.randint(0, 4)])
print(extraValuestr)