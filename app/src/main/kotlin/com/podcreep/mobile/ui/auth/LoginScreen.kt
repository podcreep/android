package com.podcreep.mobile.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.podcreep.mobile.R

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {

  Surface {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 30.dp)
    ) {
      Image(
        painter = painterResource(R.drawable.podcreep),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 48.dp)
      )
      UsernameField(
        value = viewModel.username,
        onChange = { viewModel.setUsernameValue(it) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
      )
      PasswordField(
        value = viewModel.password,
        onChange = { viewModel.setPasswordValue(it) },
        submit = { viewModel.login() },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        isError = viewModel.isError,
        errorMessage = viewModel.errorMessage
      )
    }
  }
}

@Composable
fun UsernameField(
  value: String,
  onChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "Username",
  placeholder: String = "Enter your user name"
) {

  val focusManager = LocalFocusManager.current
  val leadingIcon = @Composable {
    Icon(
      Icons.Default.Person,
      contentDescription = "",
      tint = MaterialTheme.colorScheme.primary
    )
  }

  TextField(
    value = value,
    onValueChange = onChange,
    modifier = modifier,
    leadingIcon = leadingIcon,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions = KeyboardActions(
      onNext = { focusManager.moveFocus(FocusDirection.Down) }
    ),
    placeholder = { Text(placeholder) },
    label = { Text(label) },
    singleLine = true,
    visualTransformation = VisualTransformation.None
  )
}

@Composable
fun PasswordField(
  value: String,
  onChange: (String) -> Unit,
  submit: () -> Unit,
  modifier: Modifier = Modifier,
  isError: Boolean,
  errorMessage: String,
  label: String = "Password",
  placeholder: String = "Enter your password"
) {

  var isPasswordVisible by remember { mutableStateOf(false) }

  val leadingIcon = @Composable {
    Icon(
      Icons.Default.Lock,
      contentDescription = "",
      tint = MaterialTheme.colorScheme.primary
    )
  }
  val trailingIcon = @Composable {
    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
      Icon(
        if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
        contentDescription = "",
        tint = MaterialTheme.colorScheme.primary
      )
    }
  }
  val errorIcon = @Composable {
    Icon(
      Icons.Default.Error,
      contentDescription = "Error",
      tint = MaterialTheme.colorScheme.error)
  }

  TextField(
    value = value,
    onValueChange = onChange,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = if (isError) errorIcon else trailingIcon,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done,
      keyboardType = KeyboardType.Password
    ),
    keyboardActions = KeyboardActions(
      onDone = { submit() }
    ),
    placeholder = { Text(placeholder) },
    supportingText = {
      if (isError) Text(
        modifier = Modifier.fillMaxWidth(),
        text = errorMessage,
        color = MaterialTheme.colorScheme.error
      )
    },
    label = { Text(label) },
    singleLine = true,
    visualTransformation = if (isPasswordVisible) {
      VisualTransformation.None
    } else {
      PasswordVisualTransformation()
    }
  )
}