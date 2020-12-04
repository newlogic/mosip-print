#!/bin/sh
#

mvn -Dspring.profiles.active=local -Dmosip.event.hubURL=http://13.232.228.161:9191/websub -Dmosip.event.callBackUrl="http://188.166.234.246:8088/print/callback/notifyPrint" -Dmosip.primary-language=eng -Dmosip.secondary-language=fra -Dmosip.datashare.partner.id=newlogic -Dmosip.datashare.policy.id=newlogic spring-boot:run


