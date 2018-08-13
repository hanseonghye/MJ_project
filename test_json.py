import json
import os
import glob
import time
from pprint import pprint

start_time = time.time()

# json have
# color, top-label, landmark, result-web, label
# all of these is list type

# let's save score in target_list
# number of target json data
target_list = []
fileDirList = glob.glob('C:\\PythonProject\\test\*.json')

# load json from fileDirList
def load_json():
    for fileDir in fileDirList:
        with open(fileDir) as f:
            if 'base' not in fileDir:
                temp_json = json.load(f)
                temp_dict = dict(fileName=str(os.path.basename(f.name)), jsonDict=temp_json)
                target_list.append(temp_dict)
                temp_dict = {}

# compare and give similarity score
def how_much_similar(base, target):
    score = 0
    weightL = 10
    weightW = 50
    for baseL in base['label']:
        for targetL in target['label']:
            if targetL['description'] == baseL['description']:
                print('MATCH_LABEL: ' + baseL['description'] + ' - ' + targetL['description'])
                score = score + weightL * (baseL['score'] + targetL['score'])
    for baseW in base['result-web']:
        for targetW in target['result-web']:
            if targetW['description'] == baseW['description']:
                print('MATCH_WEB: ' + baseW['description'] + ' - ' + targetW['description'])
                score = score + weightW * (baseW['score'] + targetW['score'])
    return score

# load base & target json data
with open('C:/PythonProject/test/base.json') as f:
    base = json.load(f)
load_json()

# get score
for target in target_list:
    target['score'] = how_much_similar(base, target['jsonDict'])

# sort result
target_list = sorted(target_list, key=lambda d: (d['score']), reverse=True)

# print result
print('\n')
pprint(target_list)
print('\nbest similar: ' + target_list[0]['fileName'])

# end of program
print('\n--- %s seconds ---' % (time.time() - start_time))
