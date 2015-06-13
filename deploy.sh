#########################################################################
# File Name: deploy.sh
# Author: Lin Yiming
# mail: roymatrix.lin@gmail.com
# Created Time: 六  6/13 19:25:21 2015
#########################################################################
#!/bin/bash
mvn -Pchinacloudsites -Djavax.net.ssl.trustStore=myTrustStore clean deploy   
