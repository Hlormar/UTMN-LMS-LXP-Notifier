import requests
from datetime import datetime
import sys

s = requests.session()


def getToken(user, password):
    try:
        r = s.post("https://lms.utmn.ru/login/token.php", json={"username": user, "password": password, "service": "test"})
        return r.text.split("token")[1][3:-10]

    # connection problems
    except requests.ConnectionError:
        print("getToken: Connection error")
        return (-1)
    # unknown
    except Exception as e:
        print("getToken:", e)
        return (-1)

def getCalendar(token):
    try:
        r = s.post(url="https://lms.utmn.ru/webservice/rest/server.php?wsfunction=core_calendar_get_calendar_upcoming_view&wstoken=" + token + "&moodlewsrestformat=json")
        return r.text

    # connection problems
    except requests.ConnectionError:
        print("getCalendar: Connection error")
        return (-1)
    # empty token
    except TypeError:
        print("getCalendar: Empty token")
        return (-1)
    # unknown
    except Exception as e:
        print("getCalendar:", e)
        return (-1)


def formatDict(raw):
    try:
        # cut the long base64 image part
        courseIndex = raw.find('"courseimage"')
        progressIndex = raw.find('"progress"')
        while courseIndex != -1:
            raw = raw[:courseIndex] + raw[progressIndex:]
            progressIndex = raw.find('"progress"', courseIndex+1)
            courseIndex = raw.find('"courseimage"')

        # replace \/ with /
        slashIndex = raw.find('\\/')
        while slashIndex != -1:
            raw = raw[:slashIndex] + raw[slashIndex+1:]
            slashIndex = raw.find('\\/', slashIndex+1)

        null = "None"
        false = "False"
        true = "True"
        return (eval(raw))  # returns a dictionary

    # no data
    except AttributeError:
        print("formatDict: No data")
        return (-1)
    # unknown
    except Exception as e:
        print("formatDict:", e)
        return (-1)


def parseValues(calendar):
    # recieves a python dictionary (output of the formatDict())
    try:
        events = calendar['events']  # an array of a dictionaries
        out = ""
        for event in events:
            # replace 01.01.1970 to 0
            start = event['timestart']
            duration = event['timeduration']
            modified = event['timemodified']

            if start == 0:
                start = '0'
            else:
                start = str(datetime.fromtimestamp(start))
            if duration == 0:
                duration = '0'
            else:
                duration = str(datetime.fromtimestamp(duration))
            if modified == 0:
                modified = '0'
            else:
                modified = str(datetime.fromtimestamp(modified))

            out += ('COURSE:\t\t' + event['course']['fullnamedisplay'] +
                  '\nTASK:\t\t' + event['name'] +
                  '\nDESCRIPTION:\t\t' + event['description'] +
                  '\nEVENT TYPE:\t\t' + event['eventtype'] +
                  '\nSTART DATE:\t\t' + start +
                  '\nDURATION DATE:\t\t' + duration +
                  '\nMODIFICATION TIME:\t\t' + modified +
                  '\nCALENDAR URL:\t\t' + event['viewurl'] +
                  '\nCOURSE URL:\t\t' + event['course']['viewurl'] +
                  '\nHAS PROGRESS?:\t\t' + event['course']['hasprogress'] +
                  '\nPROGRESS:\t\t' + str(event['course']['progress']) + "\n\n")
        return out

    except TypeError:
        print("parseValue: No data or certain value")
        return(-1)
    except Exception as e:
        print("parseValue:", e)
        return(-1)

def convertTime(timestampStr):
    if timestampStr == "0":
        return "0"
    return str(datetime.fromtimestamp(int(timestampStr)))

if __name__ == '__main__':
    print(fullAlgo(sys.argv[1], sys.argv[2]))


