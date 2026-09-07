import json,re,hashlib,xml.etree.ElementTree as ET
from pathlib import Path
root=Path.cwd()
e=root/'docs/qualification/CT-V2-15R1-PRACTICAL-EVIDENCE'
raw=root/'out/ct-v2-15r1-practical'
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
run=raw/'run-01'
if not (run/'final-disposition.txt').exists(): raise SystemExit('Physical comparison pending completed capture')
lines=read(run/'fixture-continuous.txt').splitlines()
xml=ET.parse(e/'CTV215R1PracticalAdjudicationTest.xml').getroot()
stdout=xml.find('system-out').text
def jvm(marker): return next(l.split(marker,1)[1] for l in stdout.splitlines() if l.startswith(marker))
obs_marker='PRACTICAL_OBSERVATION requestMode=THERAPY requestSupport=PRACTICAL_HELP state=CoreOrdinaryTherapyState(stateId=android-therapy-state-84,'
index=next(i for i,l in enumerate(lines) if obs_marker in l)
after=lines[index:]
observation=lines[index].split(' state=',1)[1]
classification=jvm('EXACT_CLASSIFICATION=')
assert classification.startswith(observation), 'Physical classification differs from direct replay'
selected=next(l for l in lines if 'PRACTICAL_UI_SELECTED=' in l)
assert selected.endswith('PRACTICAL_HELP')
pre=next(l for l in lines if 'PRACTICAL_UI_PRE mode=THERAPY support=PRACTICAL_HELP ' in l)
pre_session=next(l for l in lines[:index] if 'PRACTICAL_UI_AFTER' in l and 'session=CoreOrdinaryTherapyState(stateId=android-therapy-state-83,' in l)
physical_pre=pre_session.split(' session=',1)[1]
direct_pre=jvm('EXACT_PRE_STATE=')
assert direct_pre.startswith(physical_pre), 'Physical pre-state differs from direct replay'
action_re=r'CoreActionExecution\(actionId=PolicyActionId\(value=([^)]*)\), conversationRevision=(\d+), route=([A-Z_]+)\)'
observed_actions=[]
for line in lines[:index]:
 if 'expectedAction=' in line and ' actual=CoreActionExecution' in line:
  action=re.search(action_re,line).groups()
  if not observed_actions or action!=observed_actions[-1]: observed_actions.append(action)
assert observed_actions==re.findall(action_re,direct_pre)
plan=next(l.split('PRACTICAL_PLAN=',1)[1] for l in after if 'PRACTICAL_PLAN=' in l)
assert 'selectedActionId=core-verify-problem-understanding' in plan
assert 'route=PRACTICAL_PROBLEM_SOLVING' in plan
assert 'candidateCount=0, selectedCount=0' in plan
assert 'memoryUseDisposition=NO_RELEVANT_MEMORY' in plan
physical_render=next(l.split('PRACTICAL_RENDER_RESULT=',1)[1] for l in after if 'PRACTICAL_RENDER_RESULT=' in l)
direct_render=jvm('EXACT_RENDER_RESULT=')
assert physical_render==direct_render, 'Complete render result differs between physical and direct control'
command=next(l.split('PRACTICAL_RENDER_COMMAND=',1)[1] for l in after if 'PRACTICAL_RENDER_COMMAND=' in l)
assert 'semanticAct=CLARIFYING_QUESTION' in command
assert 'I might be understanding this as arranging a new UI meeting. Is that right?' in command
ui_after=next(l for l in after if 'PRACTICAL_UI_AFTER' in l)
assert 'status=Rendering unavailable support=PRACTICAL_HELP' in ui_after
identity=next(l for l in after if 'UI_IDENTITY' in l)
assert 'allocated=84' in identity and 'committed=true NOT_SAVED=false' in identity
digest=re.search(r'canonicalRenderDigest=([0-9a-f]+)',physical_render)[1]
result={
 'adjudication':'CORRECT_PRACTICAL_SELECTION_THEN_RENDERER_OPENING_EXHAUSTION',
 'uiSelected':'PRACTICAL_HELP','viewModelSupport':'PRACTICAL_HELP','requestSupport':'PRACTICAL_HELP',
 'proceduralRoute':'PRACTICAL_PROBLEM_SOLVING','selectedAction':'core-verify-problem-understanding',
 'lastDeliveredActionBeforeAndAfter':'core-summarize-shared-understanding',
 'rendererSemanticAct':'CLARIFYING_QUESTION','rendererDisposition':'RENDERING_UNAVAILABLE',
 'rendererRejection':'REPEATED_OPENING','committedIdentity':'android-therapy-84',
 'physicalPreStateCapturedPrefixCharacters':len(physical_pre),'directPreStateCharacters':len(direct_pre),
 'physicalClassifiedStateCapturedPrefixCharacters':len(observation),'directClassifiedStateCharacters':len(classification),
 'capturedStatePrefixesExactlyMatchDirectReplay':True,'completeDeliveredHistoryMatches':True,
 'deliveredHistory':observed_actions,'completeRenderResultExactlyMatchesDirectReplay':True,'canonicalRenderDigest':digest,
 'context':'Both select zero historical candidates and no relevant memory; corpus contents/revisions/digests differ and are not claimed equal',
 'logLimit':'Long state/plan Logcat lines truncate; prefixes are compared explicitly, complete action history reconstructed from per-turn markers. The renderer result fits and is compared in full.',
 'productionRepairMade':False,'oracleExpectedActionChanged':False
}
save('physical-decision-comparison.json',result)
(e/'checkpoint-84-boundaries.txt').write_text('\n'.join([selected,pre,lines[index],plan,command,physical_render,identity,ui_after])+'\n',encoding='utf-8',newline='\n')
print(json.dumps(result,indent=2))
