■RCP版を実行する場合
	a. HinemosClient/client_switch/RCP.targetを開き、右上の「Set as Target Platform」をクリック。 (初回のみ)
	b. HinemosClient/client_switch/switch.xmlを開き、"switch_to_RCP"を実行。 (初回のみ)
	c. hinemos.productを開き、「Launch an Eclipse application」/「Launch an Eclipse application in Debug mode」をクリックして実行。
	   もし、「Select Configuration」ダイアログが現れた場合、「RCP」を選択して実行。

	※注意事項1 - RCP版実行時にextension entry存在しないエラーが出る (初回のみ)
	<対策方法>
	1) ProjectのPropertiesを開く。
	2) 「Plug-in Development」->「Plug-in Manifest Compiler」->「Enable project specific settings」を有効にする。
	3) 「Usage」->「Unresolved extension points」をerrorからwarningに変更。

■RAP版を実行する場合
	a. HinemosClient/client_switch/RAP.targetを開き、右上の「Set as Target Platform」をクリック。 (初回のみ)
	b. HinemosClient/client_switch/switch.xmlを開き、"switch_to_RAP"を実行。 (初回のみ)
	c. hinemos.productを開き、「Launch a RAP application」/「Launch a RAP application in Debug mode」をクリックして実行。
	   もし、「Select Configuration」ダイアログが現れた場合、「RAP」を選択して実行。
