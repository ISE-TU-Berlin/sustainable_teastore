{{- define "teastore.all.microservice" -}}
all
{{- end }}

{{- define "teastore.all.fullname" -}}
{{- include "teastore.fullname" . }}-{{- include "teastore.all.microservice" . }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "teastore.all.selectorLabels" -}}
{{ include "teastore.selectorLabels" . }}
app.kubernetes.io/name: {{ include "teastore.all.microservice" . }}
app.kubernetes.io/component: webserver
{{- end }}

{{- define "teastore.all.hostname" -}}
{{- if .Values.clientside_loadbalancer -}}
$(POD_NAME).{{- .Values.all.svc_name -}}
{{- else if .Values.all.url -}}
{{- .Values.all.url -}}
{{- else -}}
{{- .Values.all.svc_name -}}
{{- end -}}
{{- end -}}
