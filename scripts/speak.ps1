param([string]$Message)

Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;
using Microsoft.Win32;

public enum ERole { eConsole = 0, eMultimedia = 1, eCommunications = 2 }

[ComImport]
[Guid("F8679F50-850A-41CF-9C72-430F290290C8")]
[InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
internal interface IPolicyConfig
{
    [PreserveSig] int GetMixFormat(string pszDeviceName, IntPtr ppFormat);
    [PreserveSig] int GetDeviceFormat(string pszDeviceName, bool bDefault, IntPtr ppFormat);
    [PreserveSig] int ResetDeviceFormat(string pszDeviceName);
    [PreserveSig] int SetDeviceFormat(string pszDeviceName, IntPtr pEndpointFormat, IntPtr pMixFormat);
    [PreserveSig] int GetProcessingPeriod(string pszDeviceName, bool bDefault, IntPtr pmftDefaultPeriod, IntPtr pmftMinimumPeriod);
    [PreserveSig] int SetProcessingPeriod(string pszDeviceName, IntPtr pmftPeriod);
    [PreserveSig] int GetShareMode(string pszDeviceName, IntPtr pMode);
    [PreserveSig] int SetShareMode(string pszDeviceName, IntPtr mode);
    [PreserveSig] int GetPropertyValue(string pszDeviceName, bool bFxStore, IntPtr key, IntPtr pv);
    [PreserveSig] int SetPropertyValue(string pszDeviceName, bool bFxStore, IntPtr key, IntPtr pv);
    [PreserveSig] int SetDefaultEndpoint(string pszDeviceName, ERole role);
    [PreserveSig] int SetEndpointVisibility(string pszDeviceName, bool bVisible);
}

[ComImport]
[Guid("870af99c-171d-4f9e-af0d-e63df40c2bc9")]
internal class CPolicyConfigClient { }

[ComImport]
[Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")]
internal class MMDeviceEnumerator { }

[ComImport]
[InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
[Guid("A95664D2-9614-4F35-A746-DE8DB63617E6")]
internal interface IMMDeviceEnumerator
{
    [PreserveSig] int EnumAudioEndpoints(int dataFlow, int stateMask, out IntPtr devices);
    [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IMMDevice device);
    [PreserveSig] int GetDevice(string id, out IMMDevice device);
    [PreserveSig] int RegisterEndpointNotificationCallback(IntPtr client);
    [PreserveSig] int UnregisterEndpointNotificationCallback(IntPtr client);
}

[ComImport]
[InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
[Guid("D666063F-1587-4E43-81F1-B948E807363F")]
internal interface IMMDevice
{
    [PreserveSig] int Activate(Guid iid, int clsCtx, IntPtr activationParams, out IntPtr pInterface);
    [PreserveSig] int OpenPropertyStore(int stgmAccess, out IntPtr pProperties);
    [PreserveSig] int GetId(out IntPtr strId);
    [PreserveSig] int GetState(out int pdwState);
}

public static class Changer
{
    public static void SetDefault(string deviceId)
    {
        IPolicyConfig pc = (IPolicyConfig)(new CPolicyConfigClient());
        pc.SetDefaultEndpoint(deviceId, ERole.eConsole);
        pc.SetDefaultEndpoint(deviceId, ERole.eMultimedia);
        pc.SetDefaultEndpoint(deviceId, ERole.eCommunications);
    }

    public static string GetDefaultRenderId()
    {
        try
        {
            IMMDeviceEnumerator en = (IMMDeviceEnumerator)(new MMDeviceEnumerator());
            IMMDevice dev;
            int hr = en.GetDefaultAudioEndpoint(0, 0, out dev);
            if (hr < 0 || dev == null) return null;
            IntPtr pid;
            hr = dev.GetId(out pid);
            if (hr < 0 || pid == IntPtr.Zero) return null;
            string id = Marshal.PtrToStringUni(pid);
            Marshal.FreeCoTaskMem(pid);
            return id;
        }
        catch { return null; }
    }

    public static string FindSpeakersId()
    {
        string path = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\MMDevices\\Audio\\Render";
        using (RegistryKey render = Registry.LocalMachine.OpenSubKey(path))
        {
            if (render == null) return null;
            foreach (string name in render.GetSubKeyNames())
            {
                using (RegistryKey dev = render.OpenSubKey(name))
                {
                    if (dev == null) continue;
                    object stateObj = dev.GetValue("DeviceState");
                    if (stateObj == null || (int)stateObj != 1) continue;
                    using (RegistryKey props = dev.OpenSubKey("Properties"))
                    {
                        if (props == null) continue;
                        object fn = props.GetValue("{a45c254e-df1c-4efd-8020-67d146a850e0},2");
                        object dd = props.GetValue("{b3f8fa53-0004-438e-9003-51a46e139bfc},6");
                        string friendly = fn == null ? null : fn.ToString();
                        string desc = dd == null ? null : dd.ToString();
                        if (friendly != null
                            && friendly.Equals("Speakers", StringComparison.OrdinalIgnoreCase)
                            && desc != null
                            && desc.IndexOf("Realtek", StringComparison.OrdinalIgnoreCase) >= 0)
                        {
                            return "{0.0.0.00000000}." + name;
                        }
                    }
                }
            }
        }
        return null;
    }
}
"@

$speakers = [Changer]::FindSpeakersId()
$orig = [Changer]::GetDefaultRenderId()
$switched = ($speakers -and $orig -and $speakers -ne $orig)
if ($switched) { [Changer]::SetDefault($speakers) }
try {
    Add-Type -AssemblyName System.Speech
    $t = New-Object System.Speech.Synthesis.SpeechSynthesizer
    $t.SelectVoice("Microsoft Zira Desktop")
    $t.Speak($Message)
} finally {
    if ($switched) { [Changer]::SetDefault($orig) }
}