import React from "react";
import { Repository } from "@scm-manager/ui-types";
import { CardColumn, DateFromNow } from "@scm-manager/ui-components";
import RepositoryEntryLink from "./RepositoryEntryLink";
import RepositoryAvatar from "./RepositoryAvatar";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

type Props = {
  repository: Repository;
};

class RepositoryEntry extends React.Component<Props> {
  createLink = (repository: Repository) => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  renderBranchesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["branches"]) {
      return <RepositoryEntryLink icon="code-branch" to={repositoryLink + "/branches"} />;
    }
    return null;
  };

  renderChangesetsLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["changesets"]) {
      return <RepositoryEntryLink icon="exchange-alt" to={repositoryLink + "/code/changesets/"} />;
    }
    return null;
  };

  renderSourcesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["sources"]) {
      return <RepositoryEntryLink icon="code" to={repositoryLink + "/code/sources/"} />;
    }
    return null;
  };

  renderModifyLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["update"]) {
      return <RepositoryEntryLink icon="cog" to={repositoryLink + "/settings/general"} />;
    }
    return null;
  };

  createFooterLeft = (repository: Repository, repositoryLink: string) => {
    return (
      <>
        {this.renderBranchesLink(repository, repositoryLink)}
        {this.renderChangesetsLink(repository, repositoryLink)}
        {this.renderSourcesLink(repository, repositoryLink)}
        <ExtensionPoint name={"repository.card.quickLink"} props={{ repository, repositoryLink }} renderAll={true} />
        {this.renderModifyLink(repository, repositoryLink)}
      </>
    );
  };

  createFooterRight = (repository: Repository) => {
    return (
      <small className="level-item">
        <DateFromNow date={repository.creationDate} />
      </small>
    );
  };

  createTitle = () => {
    const { repository } = this.props;
    return (
      <>
        <ExtensionPoint name="repository.card.beforeTitle" props={{ repository }} /> <strong>{repository.name}</strong>
      </>
    );
  };

  render() {
    const { repository } = this.props;
    const repositoryLink = this.createLink(repository);
    const footerLeft = this.createFooterLeft(repository, repositoryLink);
    const footerRight = this.createFooterRight(repository);
    const title = this.createTitle();
    return (
      <CardColumn
        avatar={<RepositoryAvatar repository={repository} />}
        title={title}
        description={repository.description}
        link={repositoryLink}
        footerLeft={footerLeft}
        footerRight={footerRight}
      />
    );
  }
}

export default RepositoryEntry;
