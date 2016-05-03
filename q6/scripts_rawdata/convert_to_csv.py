filelist = ["filesize_msexcel.csv","filesize_msword.csv","filesize_paper.csv","filesize_tikamsoffice.csv","filesize_xmldata.csv","filesize_rssxmldata.csv"]
for item in filelist:
	f = open(item,"r")
	for line in f:
		p = line.split(' ')
		a = p[0]
		b = p[1]
		f1 = open(item.split('_')[1],"a")
		f1.write(a+","+b+"\n")
		f1.close()
	f.close()
