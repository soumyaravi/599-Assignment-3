f = open("xmldata.csv",'r')
for line in f:
	p = line.split(' ')
	filesize = p[1]
	text = p[2]
	met = p[3]
	sum1 = int(filesize) + int(text) + int(met)
	normfilesize = (float(filesize)/float(sum1)) *100
	normtext = (float(text)/float(sum1)) *100
	normmet = (float(met)/float(sum1))*100
	print str(normfilesize) + "	"+str(normtext)+"	"+str(normmet)
f.close()
