import 'package:flutter/material.dart';
import 'dart:async';

import 'package:device_id/device_id.dart';
import 'package:flutter/services.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _deviceid = 'Unknown';
  String _gsf = 'Unknown';
  String _macAddress = 'Unknown';

  @override
  void initState() {
    super.initState();
    initDeviceId();
  }

  Future<void> initDeviceId() async {
    String deviceid;
    String imei;
    String meid;
    String gsf;
    String macAddress;

    deviceid = await DeviceId.getID;
    try {
      imei = await DeviceId.getIMEI;
      meid = await DeviceId.getMEID;
      gsf = await DeviceId.getGsf;
      macAddress = await DeviceId.getMacAddress;
    } on PlatformException catch (e) {
      print(e.message);
    }

    if (!mounted) return;

    setState(() {
      _deviceid =
          'Your deviceid: $deviceid\nYour IMEI: $imei\nYour MEID: $meid';
      _gsf = 'GSF: $gsf';
      _macAddress = 'MAC ADDRESS: $macAddress';
    });
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Device Id example app'),
        ),
        body: new Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            new Center(
              child: new Text('$_deviceid'),
            ),
            new Center(
              child: new Text('$_gsf'),
            ),
            new Center(
              child: new Text('$_macAddress'),
            ),
          ],
        ),
      ),
    );
  }
}
