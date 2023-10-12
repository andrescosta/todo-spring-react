import pathlib
import sys
files = [f for f in pathlib.Path(sys.argv[1]).iterdir() if f.is_file()]

for f in files:
    nf = open(f.name +"-map.yaml","x+t")
    nf.write(
 f"""apiVersion: v1
data:
  {f.name}: |
    {'    '.join(f.read_text().splitlines(True))}
kind: ConfigMap
metadata:
  name: {sys.argv[2]}-configmap
"""
    )
    nf.close()