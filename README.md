<h1 align="center">TeamKUNPluginManager</h1>

<p align="center">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/TeamKun/TeamKUNPluginManager/maven.yml?style=flat-square">
    <a href="https://www.codacy.com/gh/TeamKun/TeamKUNPluginManager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TeamKun/TeamKUNPluginManager&amp;utm_campaign=Badge_Grade">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/de19f8162c394e46b56db749a35df467?logo=codacy&style=flat-square">
    </a>
    <img alt="GitHub" src="https://img.shields.io/github/license/TeamKun/TeamKunPluginManager?style=flat-square">
    <img alt="Java version" src="https://img.shields.io/static/v1?label=Java%20version&message=1.8&color=success&style=flat-square">
    <a href="https://kpm.kunlab.org">
        <img alt="Docs" src="https://img.shields.io/static/v1?label=Docs&message=Website%E2%98%81&color=green&style=flat-square">
    </a>
    <br>
    リポジトリからプラグインおよびその依存関係を自動でダウンロードし, 高度なサーバ管理支援を行います。
    <br>
    It automatically downloads plugins and their dependencies from repositories and provides advanced server management support.
</p>

## 概要 (Overview)

* TeamKUNPluginManager は、PaperMC プラグインを簡単かつ安全にインストールするツールです。
* GitHub や HTTP サーバなどのリポジトリから必要なプラグインを簡単な操作で自動ダウンロードし、インストールできます。
* KPM は、自動で依存関係を解決するため、手動で前提プラグインをインストールする必要がなくなります。
* KPMは不要なプラグイン（削除されたプラグインの依存関係として自動インストールされたもの等）を自動で検出し、管理者に報告します。  
  これにより、不要なプラグインを削除でき、パフォーマンスの向上につながります。
* 依存関係ツリーとメタデータを自動で構築し、プラグインの管理が簡単になります。
* グラフィカルなインタフェースで、必要なプラグインをより簡単に見つけられます。

## 言語サポート (Language Support)

KPM は、以下の言語に対応しています。

| 言語 (Language) | プラグイン (Plugin)     | ドキュメント (Documentation) |
|:--------------|:-------------------|:-----------------------|
| 日本語           | :white_check_mark: | :white_check_mark:     |
| English(US)   | :white_check_mark: | :x:                    |
| 日本語(関西弁)      | :white_check_mark: | :x:                    |

## 導入方法とドキュメント (Installation and Documentation)

* [こちら](https://kpm.kunlab.org) を参照してください。

## スクリーンショット (Screenshots)

<img src="images/pl-install.png" width="350px" alt="Install feature">
<img src="images/pl-upgrade.png" width="350px" alt="Upgrade feature">
<br>
<img src="images/clean.png" width="350px" alt="Clean feature">
<img src="images/pl-info.png" width="350px" alt="Plugin info
feature">
