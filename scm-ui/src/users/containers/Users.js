// @flow
import React from "react";
import { connect } from "react-redux";
import classNames from "classnames";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import type { History } from "history";
import queryString from "query-string";
import type { User, PagedCollection } from "@scm-manager/ui-types";
import {
  fetchUsersByPage,
  getUsersFromState,
  selectListAsCollection,
  isPermittedToCreateUsers,
  isFetchUsersPending,
  getFetchUsersFailure
} from "../modules/users";
import {
  Page,
  PageActions,
  Button,
  CreateButton,
  Notification,
  LinkPaginator,
  getPageFromMatch,
  FilterInput
} from "@scm-manager/ui-components";
import { UserTable } from "./../components/table";
import { getUsersLink } from "../../modules/indexResource";

type Props = {
  users: User[],
  loading: boolean,
  error: Error,
  canAddUsers: boolean,
  list: PagedCollection,
  page: number,
  usersLink: string,

  // context objects
  classes: Object,
  t: string => string,
  history: History,
  location: any,

  // dispatch functions
  fetchUsersByPage: (link: string, page: number, filter?: string) => void
};

const styles = {
  button: {
    float: "right",
    marginTop: "1.25rem"
  }
};

class Users extends React.Component<Props> {
  componentDidMount() {
    const { fetchUsersByPage, usersLink, page } = this.props;
    fetchUsersByPage(usersLink, page, this.getQueryString());
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      list,
      page,
      loading,
      location,
      fetchUsersByPage,
      usersLink
    } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchUsersByPage(usersLink, page, this.getQueryString());
      }
    }
  };

  render() {
    const { users, loading, error, t } = this.props;
    return (
      <Page
        title={t("users.title")}
        subtitle={t("users.subtitle")}
        loading={loading || !users}
        error={error}
      >
        {this.renderUserTable()}
        {this.renderCreateButton()}
        {this.renderPageActions()}
      </Page>
    );
  }

  renderUserTable() {
    const { users, t } = this.props;
    if (users && users.length > 0) {
      return (
        <>
          <UserTable users={users} />
          {this.renderPaginator()}
        </>
      );
    }
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  renderPaginator = () => {
    const { list, page } = this.props;
    if (list) {
      return (
        <LinkPaginator
          collection={list}
          page={page}
          filter={this.getQueryString()}
        />
      );
    }
    return null;
  };

  renderCreateButton() {
    const { canAddUsers, t } = this.props;
    if (canAddUsers) {
      return <CreateButton label={t("users.createButton")} link="/users/add" />;
    }
    return null;
  }

  renderPageActions() {
    const { canAddUsers, history, classes, t } = this.props;
    if (canAddUsers) {
      return (
        <PageActions>
          <FilterInput
            value={this.getQueryString()}
            filter={filter => {
              history.push("/users/?q=" + filter);
            }}
          />
          <div className={classNames(classes.button, "input-button control")}>
            <Button
              label={t("users.createButton")}
              link="/users/add"
              color="primary"
            />
          </div>
        </PageActions>
      );
    }

    return null;
  }

  getQueryString = () => {
    const { location } = this.props;
    return location.search ? queryString.parse(location.search).q : undefined;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const users = getUsersFromState(state);
  const loading = isFetchUsersPending(state);
  const error = getFetchUsersFailure(state);
  const page = getPageFromMatch(match);
  const canAddUsers = isPermittedToCreateUsers(state);
  const list = selectListAsCollection(state);
  const usersLink = getUsersLink(state);

  return {
    users,
    loading,
    error,
    canAddUsers,
    list,
    page,
    usersLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUsersByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchUsersByPage(link, page, filter));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(injectSheet(styles)(translate("users")(Users)));
