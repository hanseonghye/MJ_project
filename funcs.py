import os
from pprint import pprint

# compare and give similarity score
def how_much_similar(base, target):
    score = 0
    weightL = 10
    weightW = 10
    for baseL in base['label']:
        for targetL in target['label']:
            if len(baseL['description']) != 0 and len(targetL['description']) != 0 and targetL['description'] == baseL['description']:
                print('MATCH_LABEL:' + baseL['description'] + '-' + targetL['description'])
                score = score + weightL * (baseL['score'] + targetL['score'])
    for baseW in base['result-web']:
        for targetW in target['result-web']:
            if len(baseW['description']) != 0 and len(targetW['description']) != 0 and targetW['description'] == baseW['description']:
                print('MATCH_WEB:' + baseW['description'] + '-' + targetW['description'])
                score = score + weightW * (baseW['score'] + targetW['score'])
    return score

def print_result(target_list):
    # print result
    print('### RESULT FOR SIMILARITY ###')
    print('FILE NAME\t\tSCORE\n')
    for target in target_list:
        print(target['fileName'] + '\t\t' + str(target['score']) + '\n')
    # print the best
    print('*** best 3: ')
    best = []
    for i in range(0, 3):
        temp = os.path.splitext(target_list[i]['fileName'])
        temp = os.path.split(temp[0])
        best.append(temp[1] + '.txt')
        print(best[i])
    return best

# called at app.py
def get_score(base, target_list):
    print('### CALCULATE SIMILARITY SCORE ###')
    for target in target_list:
        print('*** TARGET: ' + target['fileName'])
        target['score'] = how_much_similar(base, target['jsonDict'])
        print('---------------------------------------------------')
    # sort result
    target_list = sorted(target_list, key=lambda d: (d['score']), reverse=True)
    # print and return result
    return print_result(target_list)
