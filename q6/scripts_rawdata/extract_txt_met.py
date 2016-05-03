import os
import subprocess
import codecs

folder_list = ['/media/jaydeep/mySpace/spring2016/599/tikamsoffice/']
txt_temp = "/home/jaydeep/"
for item in folder_list:
	for i in  os.listdir(item):
		try:
			command = "java -jar /home/jaydeep/tika-1.12/tika-app/target/tika-app-1.12.jar -t "+item + i
			result = subprocess.check_output(command, shell=True)
			f1 = codecs.open(txt_temp + "temp_extract_txt", "w","utf-8")
			f1.write(result)
			f1.close()

			command = "java -jar /home/jaydeep/tika-1.12/tika-app/target/tika-app-1.12.jar -m "+item + i
			result = subprocess.check_output(command, shell=True)
			f1 = codecs.open(txt_temp + "temp_extract_meta", "w","utf-8")
			f1.write(result)
			f1.close()
			parser_call_chain = []
			f2 = codecs.open(txt_temp + "temp_extract_meta", "r")
			for line in f2:
				#X-Parsed-By: org.apache.tika.parser.DefaultParser
				p = line.split(':')
				if p[0] == "X-Parsed-By":
					parser_call_chain.append(p[1].lstrip(' '))
			f2.close()

			command = "stat --printf='%s' "+item + i
			filesize = subprocess.check_output(command, shell=True)

			command = "stat --printf='%s' /home/jaydeep/temp_extract_txt"
			extracted_txt_size = subprocess.check_output(command, shell=True)

			command = "stat --printf='%s' /home/jaydeep/temp_extract_meta"
			extracted_meta_size = subprocess.check_output(command, shell=True)

			f4 = open("/home/jaydeep/599hw3/"+item.split('/')[-2],"a")
			f4.write(i + " " + filesize + " " + extracted_txt_size + " " + extracted_meta_size + " " + str(parser_call_chain)+"\n")
			f4.close()

		except:
			continue
