import json,re,hashlib,xml.etree.ElementTree as ET
from pathlib import Path
root=Path.cwd()
e=root/'docs/qualification/CT-V2-15R1-OPENING-EVIDENCE'
raw=root/'out/ct-v2-15r1-opening'
def read(p):
 b=p.read_bytes()
 return b.decode('utf-16') if b.startswith((b'\xff\xfe',b'\xfe\xff')) else b.decode('utf-8-sig')
def sha(b): return hashlib.sha256(b).hexdigest()
def save(name,obj): (e/name).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8',newline='\n')
for run in sorted(raw.glob('run-*')):
 if not (run/'final-disposition.txt').exists(): continue
 out=e/run.name; out.mkdir(exist_ok=True)
 manifest=[]
 for p in sorted(run.iterdir()):
  if not p.is_file(): continue
  b=p.read_bytes(); item={'file':p.name,'rawBytes':len(b),'rawSHA256':sha(b)}
  if p.suffix!='.apk':
   normalized=('\n'.join(line.rstrip() for line in read(p).splitlines()).rstrip()+'\n').encode()
   (out/p.name).write_bytes(normalized)
   item.update(evidenceSHA256=sha(normalized),evidenceBytes=len(normalized))
  else: item['retention']='APK retained in ignored raw physical capture'
  manifest.append(item)
 (out/'capture-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8',newline='\n')
 print('PRESERVED',run.name,len(manifest))

summary={}
for run in sorted(raw.glob('run-*')):
 if not (run/'final-disposition.txt').exists(): continue
 disposition=read(run/'final-disposition.txt')
 methods={}
 for p in sorted(run.glob('instrumentation-*.txt')):
  text=read(p); duration=re.search(r'Time: ([0-9.,]+)',text)
  methods[p.stem.removeprefix('instrumentation-')]={'passed':'OK (1 test)' in text,'seconds':float(duration[1].replace(',','')) if duration else None}
 summary[run.name]={'methods':methods,'custodyUnchanged':'CANONICAL_CUSTODY_UNCHANGED=true' in disposition,'disposablePackagesRemaining':0 if 'DISPOSABLE_PACKAGES_REMAINING=0' in disposition else None,'allMethodsPassed':'PHYSICAL_ALL_METHODS_PASSED=True' in disposition}
save('physical-summary.json',summary)
full=raw/'run-02'
if (full/'final-disposition.txt').exists():
 lines=read(full/'fixture-continuous.txt').splitlines()
 evidence=[l for l in lines if any(m in l for m in ['R1 CHECKPOINT16','R1 UI_IDENTITY','R1 UI_REOPEN_DURABILITY','R1 UI_DURABILITY_EXPECT','R1 UI_CONTROLS_COMPLETE','R1 UI_FIRST_IDENTITY','R1 UI_SECOND_REOPEN'])]
 (e/'physical-checkpoint-and-identity.txt').write_text('\n'.join(evidence)+'\n',encoding='utf-8')
 ids=[int(m[1]) for l in lines if 'R1 UI_IDENTITY' in l for m in [re.search(r' allocated=(\d+)',l)] if m]
 save('physical-ui-identities.json',{'identities':ids,'unique':len(ids)==len(set(ids)),'consecutive':all(b==a+1 for a,b in zip(ids,ids[1:]))})
print(json.dumps(summary,indent=2))
