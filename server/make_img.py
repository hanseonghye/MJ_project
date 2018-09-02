import os

from urllib2 import Request,urlopen
from urllib2 import URLError, HTTPError

Dir="URL/"
save_Dir="crw_image/"

for root, dirs, files in os.walk(Dir):
    for fname in files:
        full_name=os.path.join(root, fname)
        f=open(full_name,'r')
        url=f.readline()
        f.close()

        save_name=fname.split('.')
        save_name=save_name[0]
        save_name=save_Dir+save_name+'.png'
        print ("start ... "+ save_name)
        if os.path.isfile(save_name) == False :
            req = Request(url, headers={"User-Agent": "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.27 Safari/537.17"})
            response=urlopen(req)
            real_url=response.geturl()


            output_file=open(save_name,'wb')
            data=response.read()
            output_file.write(data)
            print ("end ... "+ save_name)

print ("end.")