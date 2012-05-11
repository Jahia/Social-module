[consequence][]Add activity on {node} with message key {messageKey}=socialService.addActivityWithParameter("resourceBundle", user.getName(), {node}, {messageKey}, drools);
[consequence][]Add activity of type {activityType} on {node} with parameters {params}=socialService.addActivityWithParametersArray({activityType}, user.getName(), {node}, {params}, drools);
[consequence][]Add activity of type {activityType} on {node} with parameter {param}=socialService.addActivityWithParameter({activityType}, user.getName(), {node}, {param}, drools);
[consequence][]Add activity of type {activityType} on {node}=socialService.addActivityWithParameter({activityType}, user.getName(), {node}, null, drools);
[consequence][]Send message {message} with subject {subject} from user {fromUser} to user {toUser}=socialService.sendMessage({fromUser}, {toUser}, {subject}, {message}, drools);