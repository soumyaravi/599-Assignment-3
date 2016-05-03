filelist = ["tikamsoffice","xmldata.csv"]
for item in filelist:
	f = open(item,"r")
	for line in f:
		print line,
		p = line.split(" ")
		filesize = p[1]
		f1 = open("filesize_"+item.rstrip('.csv')+".csv","a")
		f1.write(str(filesize) + "\n")
		f1.close()	
	f.close()
