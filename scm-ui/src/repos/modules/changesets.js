// @flow

import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { combineReducers } from "redux";
import type { Action, PagedCollection } from "@scm-manager/ui-types";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

const REPO_URL = "repositories";
//TODO: Content type
// actions

export function fetchChangesetsWithOptions(
  namespace: string,
  name: string,
  branch?: string,
  suffix?: string
) {
  let link = REPO_URL + `/${namespace}/${name}`;
  if (branch && branch !== "") {
    link = link + `/branches/${branch}`;
  }
  link = link + "/changesets";
  if (suffix) {
    link = link + `${suffix}`;
  }
  return function(dispatch: any) {
    dispatch(fetchChangesetsPending(namespace, name, branch));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data, namespace, name, branch));
      })
      .catch(cause => {
        dispatch(fetchChangesetsFailure(namespace, name, cause, branch));
      });
  };
}

export function fetchChangesets(namespace: string, name: string) {
  return fetchChangesetsWithOptions(namespace, name);
}

export function fetchChangesetsByPage(
  namespace: string,
  name: string,
  page: number
) {
  return fetchChangesetsWithOptions(namespace, name, "", `?page=${page}`);
}

export function fetchChangesetsByBranchAndPage(
  namespace: string,
  name: string,
  branch: string,
  page: number
) {
  return fetchChangesetsWithOptions(namespace, name, branch, `?page=${page}`);
}

export function fetchChangesetsByNamespaceNameAndBranch(
  namespace: string,
  name: string,
  branch: string
) {
  return fetchChangesetsWithOptions(namespace, name, branch);
}

export function fetchChangesetsPending(
  namespace: string,
  name: string,
  branch?: string
): Action {
  const itemId = createItemId(namespace, name, branch);
  return {
    type: FETCH_CHANGESETS_PENDING,
    payload: itemId,
    itemId
  };
}

export function fetchChangesetsSuccess(
  changesets: any,
  namespace: string,
  name: string,
  branch?: string
): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: changesets,
    itemId: createItemId(namespace, name, branch)
  };
}

function fetchChangesetsFailure(
  namespace: string,
  name: string,
  error: Error,
  branch?: string
): Action {
  return {
    type: FETCH_CHANGESETS_FAILURE,
    payload: {
      namespace,
      name,
      branch,
      error
    },
    itemId: createItemId(namespace, name, branch)
  };
}

function createItemId(
  namespace: string,
  name: string,
  branch?: string
): string {
  let itemId = namespace + "/" + name;
  if (branch && branch !== "") {
    itemId = itemId + "/" + branch;
  }
  return itemId;
}

// reducer
function byKeyReducer(
  state: any = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const key = action.itemId;
      let oldChangesets = { [key]: {} };
      if (state[key] !== undefined) {
        oldChangesets[key] = state[key];
      }
      return {
        ...state,
        [key]: {
          byId: extractChangesetsByIds(action.payload, oldChangesets[key].byId)
        }
      };
    default:
      return state;
  }
}

function listReducer(
  state: any = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const changesets = action.payload._embedded.changesets;
      const changesetIds = changesets.map(c => c.id);
      return {
        entries: changesetIds,
        entry: {
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };
    default:
      return state;
  }
}

export default combineReducers({
  list: listReducer,
  byKey: byKeyReducer
});

function extractChangesetsByIds(data: any, oldChangesetsByIds: any) {
  const changesets = data._embedded.changesets;
  const changesetsByIds = {};

  for (let changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  for (let id in oldChangesetsByIds) {
    changesetsByIds[id] = oldChangesetsByIds[id];
  }

  return changesetsByIds;
}

//selectors
export function getChangesets(
  state: Object,
  namespace: string,
  name: string,
  branch?: string
) {
  const key = createItemId(namespace, name, branch);
  if (!state.changesets.byKey[key]) {
    return null;
  }
  return Object.values(state.changesets.byKey[key].byId);
}

export function isFetchChangesetsPending(
  state: Object,
  namespace: string,
  name: string,
  branch?: string
) {
  return isPending(
    state,
    FETCH_CHANGESETS,
    createItemId(namespace, name, branch)
  );
}

export function getFetchChangesetsFailure(
  state: Object,
  namespace: string,
  name: string,
  branch?: string
) {
  return getFailure(
    state,
    FETCH_CHANGESETS,
    createItemId(namespace, name, branch)
  );
}

const selectList = (state: Object) => {
  if (state.changesets && state.changesets.list) {
    return state.changesets.list;
  }
  return {};
};

const selectListEntry = (state: Object): Object => {
  const list = selectList(state);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (state: Object): PagedCollection => {
  return selectListEntry(state);
};