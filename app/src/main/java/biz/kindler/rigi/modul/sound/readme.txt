https://stackoverflow.com/questions/22226552/programmatically-connect-to-paired-bluetooth-speaker-and-play-audio

http://api.shoutcast.com/legacy/Top500?k=[APIKEY]&limit=5
http://yp.shoutcast.com/sbin/tunein-station.pls?id=99226864
http://yp.shoutcast.com/sbin/tunein-station.pls?id=1272062
http://yp.shoutcast.com/sbin/tunein-station.pls?id=1702206

erster param ist "tunein base"

<stationlist>
<tunein base="/sbin/tunein-station.pls" base-m3u="/sbin/tunein-station.m3u" base-xspf="/sbin/tunein-station.xspf"/>
<station name="Rádio Evangelizar AM | crosshost.com.br" mt="audio/aacp" id="1243683" br="32" genre="Spiritual" genre2="Talk" logo="http://i.radionomy.com/document/radios/2/2771/2771e4bb-459b-4962-9e9b-161eb5448cbb.png" lc="22237"/>
<station name="Dance Wave Retro!" mt="audio/mpeg" id="1057402" br="128" genre="Dance" lc="17980"/>
<station name="ANTENA1 - 94 7 FM" mt="audio/aacp" id="901725" br="56" genre="Pop" ct="MR. BIG - TO BE WITH YOU" lc="13783"/>
<station name="Radio SCOOP - Hungary" mt="audio/mpeg" id="988599" br="128" genre="Top 40" lc="11238"/>
<station name="Alpha FM 101,7" mt="audio/mpeg" id="99226864" br="128" genre="70s" logo="http://i.radionomy.com/document/radios/2/2b1f/2b1f58f7-5061-4b5f-95bc-466d82317131.gif" ct="TIAGO IORC - NOTHING BUT A SONG" lc="10768"/>
</stationlist>


https://stackoverflow.com/questions/18652583/play-from-shoutcast-url-in-android

statt wget: auf MAC: curl -0

Patrick-Kindler-PORT-iMac:Downloads sidiplasmac$ curl -0 http://yp.shoutcast.com/sbin/tunein-station.pls?id=99226864
[playlist]
numberofentries=1
File1=http://listen.shoutcast.com/alphafm101-7
Title1=Alpha FM 101,7
Length1=-1
Version=2
