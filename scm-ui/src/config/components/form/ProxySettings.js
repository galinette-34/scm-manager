// @flow
import React from "react";
import { translate } from "react-i18next";
import {
  Checkbox,
  InputField,
  Subtitle,
  AddEntryToTableField
} from "@scm-manager/ui-components";
import ProxyExcludesTable from "../table/ProxyExcludesTable";

type Props = {
  proxyPassword: string,
  proxyPort: number,
  proxyServer: string,
  proxyUser: string,
  enableProxy: boolean,
  proxyExcludes: string[],
  t: string => string,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean
};

class ProxySettings extends React.Component<Props> {
  render() {
    const {
      t,
      proxyPassword,
      proxyPort,
      proxyServer,
      proxyUser,
      enableProxy,
      proxyExcludes,
      hasUpdatePermission
    } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("proxy-settings.name")} />
        <Checkbox
          checked={enableProxy}
          label={t("proxy-settings.enable-proxy")}
          onChange={this.handleEnableProxyChange}
          disabled={!hasUpdatePermission}
          helpText={t("help.enableProxyHelpText")}
        />
        <InputField
          label={t("proxy-settings.proxy-password")}
          onChange={this.handleProxyPasswordChange}
          value={proxyPassword}
          type="password"
          disabled={!enableProxy || !hasUpdatePermission}
          helpText={t("help.proxyPasswordHelpText")}
        />
        <InputField
          label={t("proxy-settings.proxy-port")}
          value={proxyPort}
          onChange={this.handleProxyPortChange}
          disabled={!enableProxy || !hasUpdatePermission}
          helpText={t("help.proxyPortHelpText")}
        />
        <InputField
          label={t("proxy-settings.proxy-server")}
          value={proxyServer}
          onChange={this.handleProxyServerChange}
          disabled={!enableProxy || !hasUpdatePermission}
          helpText={t("help.proxyServerHelpText")}
        />
        <InputField
          label={t("proxy-settings.proxy-user")}
          value={proxyUser}
          onChange={this.handleProxyUserChange}
          disabled={!enableProxy || !hasUpdatePermission}
          helpText={t("help.proxyUserHelpText")}
        />
        <ProxyExcludesTable
          proxyExcludes={proxyExcludes}
          onChange={(isValid, changedValue, name) =>
            this.props.onChange(isValid, changedValue, name)
          }
          disabled={!enableProxy || !hasUpdatePermission}
        />
        <AddEntryToTableField
          addEntry={this.addProxyExclude}
          disabled={!enableProxy || !hasUpdatePermission}
          buttonLabel={t("proxy-settings.add-proxy-exclude-button")}
          fieldLabel={t("proxy-settings.add-proxy-exclude-textfield")}
          errorMessage={t("proxy-settings.add-proxy-exclude-error")}
        />
      </div>
    );
  }

  handleProxyPasswordChange = (value: string) => {
    this.props.onChange(true, value, "proxyPassword");
  };
  handleProxyPortChange = (value: string) => {
    this.props.onChange(true, value, "proxyPort");
  };
  handleProxyServerChange = (value: string) => {
    this.props.onChange(true, value, "proxyServer");
  };
  handleProxyUserChange = (value: string) => {
    this.props.onChange(true, value, "proxyUser");
  };
  handleEnableProxyChange = (value: string) => {
    this.props.onChange(true, value, "enableProxy");
  };

  addProxyExclude = (proxyExcludeName: string) => {
    if (this.isProxyExcludeMember(proxyExcludeName)) {
      return;
    }
    this.props.onChange(
      true,
      [...this.props.proxyExcludes, proxyExcludeName],
      "proxyExcludes"
    );
  };

  isProxyExcludeMember = (proxyExcludeName: string) => {
    return this.props.proxyExcludes.includes(proxyExcludeName);
  };
}

export default translate("config")(ProxySettings);