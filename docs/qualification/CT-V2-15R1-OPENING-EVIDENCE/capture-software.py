import json, hashlib, zipfile, re
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path.cwd()
dest=root/'docs/qualification/CT-V2-15R1-OPENING-EVIDENCE'
def save(name,obj): (dest/name).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
suites=[]
for p in sorted(root.glob('**/build/test-results/*/TEST-*.xml')):
 r=ET.parse(p).getroot()
 suites.append(dict(path=p.relative_to(root).as_posix(),name=r.get('name'),**{k:int(r.get(k,0)) for k in ['tests','failures','errors','skipped']}))
summary={'suites':len(suites),**{k:sum(s[k] for s in suites) for k in ['tests','failures','errors','skipped']}}
save('suite-inventory.json',suites)
lint=[]
for p in sorted(root.glob('**/build/reports/lint-results*.xml')):
 r=ET.parse(p).getroot()
 lint.extend({'report':p.relative_to(root).as_posix(),'id':i.get('id'),'severity':i.get('severity'),'message':i.get('message')} for i in r.findall('issue'))
save('lint-issues.json',lint)
summary['lint']={severity:sum(i['severity']==severity for i in lint) for severity in ['Error','Fatal','Warning']}
assert summary['tests']==1014 and summary['suites']==66,summary
assert all(summary[k]==0 for k in ['failures','errors','skipped'])
assert summary['lint']=={'Error':0,'Fatal':0,'Warning':17},summary
save('automated-summary.json',summary)
artifacts=[]
for name in ['app/build/outputs/apk/debug/app-debug.apk','app/build/outputs/apk/release/app-release-unsigned.apk']:
 p=root/name
 with zipfile.ZipFile(p) as z:
  names=z.namelist()
  models=[n for n in names if re.search(r'\.(gguf|safetensors|onnx|tflite|pte)$',n,re.I)]
  markers=[n for n in names if re.search(r'(^|/)(lib(llama|ggml|onnxruntime|tensorflowlite|executorch|pytorch|whisper)[^/]*|[^/]*inference[^/]*)$',n,re.I)]
  vcs={n:z.read(n).decode('utf-8') for n in names if n.endswith('version-control-info.textproto')}
 artifacts.append(dict(path=name,bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest(),modelArtifacts=models,inferenceMarkers=markers,versionControl=vcs))
 assert not models and not markers
save('canonical-artifacts.json',artifacts)
for suffix in ['CTV215R1OpeningExhaustionTest','CTV215R1PracticalAdjudicationTest','CTV215R1DurableTurnIdentityTest','CTV215R1ReopenIdentityDiagnosticTest','CTV215R1RecurrenceRuntimeTest','CTV215R1RendererRecurrenceTest']:
 p=root/f'qualification/build/test-results/test/TEST-com.conundrum.thomas.v2.qualification.{suffix}.xml'
 (dest/(suffix+'.xml')).write_bytes(p.read_bytes())
(dest/'canonical-build.txt').write_text((root.parent/'canonical-software-qualification.txt').read_text(encoding='utf-16'),encoding='utf-8')
save('candidate-source-hashes.json',[dict(path=p.relative_to(root).as_posix(),sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in [
 root/'thomas/runtime/src/main/kotlin/com/conundrum/thomas/v2/runtime/ThomasProductionRuntime.kt',
 root/'app/src/main/java/com/conundrum/thomas/v2/ThomasViewModel.kt',
 root/'app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionDeviceInstrumentedTest.kt',
 root/'app/src/androidTest/java/com/conundrum/thomas/v2/CTV215R1ProductionUiInstrumentedTest.kt']])
print(json.dumps(summary))
print(json.dumps(artifacts,indent=2))
