// @flow

import React from "react";
import { withRouter } from "react-router-dom";
import type {
  Branch,
  Changeset,
  PagedCollection,
  Repository
} from "@scm-manager/ui-types";
import {
  fetchChangesets,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
} from "../modules/changesets";

import { connect } from "react-redux";
import ChangesetList from "../components/changesets/ChangesetList";
import { ErrorPage, LinkPaginator, Loading } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository, //TODO: Do we really need/want this here?
  branch: Branch,
  page: number,

  // State props
  changesets: Changeset[],
  list: PagedCollection,
  loading: boolean,
  error: Error,

  // Dispatch props
  fetchChangesets: (Repository, Branch, number) => void,

  // Context Props
  t: string => string
};

class Changesets extends React.Component<Props> {
  componentDidMount() {
    const { fetchChangesets, repository, branch, page } = this.props;

    fetchChangesets(repository, branch, page);
  }

  render() {
    const { changesets, loading, error, t } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("changesets.error-title")}
          subtitle={t("changesets.error-title")}
          error={error}
        />
      );
    }

    if (loading) {
      return <Loading />;
    }
    if (!changesets || changesets.length === 0) {
      return null;
    }
    return (
      <>
        {this.renderList()}
        {this.renderPaginator()}
      </>
    );
  }

  renderList = () => {
    const { repository, changesets } = this.props;
    return <ChangesetList repository={repository} changesets={changesets} />;
  };

  renderPaginator = () => {
    const { list } = this.props;
    if (list) {
      return <LinkPaginator collection={list} />;
    }
    return null;
  };
}

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesets: (repo: Repository, branch: Branch, page: number) => {
      dispatch(fetchChangesets(repo, branch, page));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, branch, match } = ownProps;
  const changesets = getChangesets(state, repository, branch);
  const loading = isFetchChangesetsPending(state, repository, branch);
  const error = getFetchChangesetsFailure(state, repository, branch);
  const list = selectListAsCollection(state, repository, branch);

  // TODO
  const page = parseInt(match.params.page || "1");

  return { changesets, list, page, loading, error };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(Changesets))
);
