# TeamKUNPluginManager

プラグインのインストールから依存関係の調達、不要になったプラグインの削除まで全て自動化します。

## 概要

プラグインの管理を行います。

## コマンド

### /kunpluginmanager

#### エイリアス

+ kpm
+ pm
+ kunpm
+ kunmgmt <= devicemgmt.mscへの恨み

#### 使用法

+ /kpm <i|rm|info|autoremove|clean|fix|status|update|import|export> [Plugin download url|GitHub repository url]

#### サブコマンド

+ install リポジトリまたはURLからインストールします。 権限: kpm.install
+ remove プラグインをアンインストールします。 権限: kpm.uninstall
+ autoremove いらないプラグインを自動で削除します。 権限: kpm.autoremove
+ update 既知プラグインデータセットをアップデートします。 権限: kpm.update
+ status 現在の状態を表示します。 権限: kpm.status
+ info プラグインの情報を取得します。 権限: kpm.info
+ fix エラーを修復します。メッセージがあった場合のみ実行してください。 権限: kpm.fix
+ clean 不要になったプラグインデータを削除します。 権限: kpm.clean
+ export プラグインをエクスポートします。 権限: kpm.export
+ import プラグインをインポートします。 権限: kpm.import

#### 権限

+ kpm.use => すべてのサブコマンドの掌握権を持ちます。

## 管理

当プラグインは、SQLite3による、依存関係ツリーの構築によって実現しています。 プラグインが有効になった時に高速で構築されます。

## 謝辞

当プラグインは、以下のオープンソース、API、サービスを使用しています。

+ [brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP)
+ [G00fY2/version-compare](https://github.com/G00fY2/version-compare)
+ [r-clancy/PlugMan](https://github.com/r-clancy/PlugMan)
+ [Apache Commons/Commons IO](https://commons.apache.org/proper/commons-io/)

---

+ [JitPack](https://jitpack.io/)
+ [file.io](https://file.io/)
